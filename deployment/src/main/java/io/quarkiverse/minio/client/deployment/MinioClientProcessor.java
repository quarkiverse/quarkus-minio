package io.quarkiverse.minio.client.deployment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.minio.BaseArgs;
import io.minio.MinioClient;
import io.quarkiverse.minio.client.EmptyHttpClientProducer;
import io.quarkiverse.minio.client.MinioBuildTimeConfiguration;
import io.quarkiverse.minio.client.MinioClients;
import io.quarkiverse.minio.client.MinioQualifier;
import io.quarkiverse.minio.client.MinioRecorder;
import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkiverse.minio.client.MiniosRuntimeConfiguration;
import io.quarkiverse.minio.client.WithMetricsHttpClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

class MinioClientProcessor {

    private static final String FEATURE = "minio-client";

    public static class MetricsEnabled implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return QuarkusClassLoader.isClassPresentAtRuntime("io.micrometer.core.instrument.MeterRegistry");
        }
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void build(BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
        // Indicates that this extension would like the SSL support to be enabled
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.minio", "minio"));
    }

    @BuildStep(onlyIfNot = { MetricsEnabled.class })
    void ifMetricsAreDisabled(
            BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemBuildProducer) {
        additionalBeanBuildItemBuildProducer
                .produce(AdditionalBeanBuildItem.unremovableOf(EmptyHttpClientProducer.class));
    }

    @BuildStep(onlyIf = { MetricsEnabled.class })
    void ifMetricsAreEnabled(
            BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemBuildProducer) {
        additionalBeanBuildItemBuildProducer
                .produce(AdditionalBeanBuildItem.unremovableOf(WithMetricsHttpClientProducer.class));
    }

    @BuildStep
    void registerForReflection(
            CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {
        List<String> classes = getClasses("io.minio.messages", null);
        classes.addAll(getClasses("org.simpleframework.xml.core", ".*Label$"));
        classes.addAll(index.getIndex().getAllKnownSubclasses(DotName.createSimple(BaseArgs.class.getName())).stream()
                .map(ClassInfo::name)
                .map(DotName::toString)
                .collect(Collectors.toList()));
        reflectionClasses.produce(new ReflectiveClassBuildItem(true, true, classes.toArray(new String[0])));
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void produce(MiniosBuildTimeConfiguration miniosBuildTimeConfiguration,
            MiniosRuntimeConfiguration miniosRuntimeConfiguration,
            MinioRecorder minioRecorder,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanBuildItemBuildProducer,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        if (miniosBuildTimeConfiguration.getMinioClients().isEmpty()) {
            //No minio client needed
            return;
        }
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(MinioQualifier.class).build());
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(MinioClients.class)
                .setUnremovable()
                .setDefaultScope(DotNames.SINGLETON).build());
        for (Map.Entry<String, MinioBuildTimeConfiguration> entry : miniosBuildTimeConfiguration.getMinioClients().entrySet()) {
            var minioClientName = entry.getKey();
            SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                    .configure(MinioClient.class)
                    .scope(Singleton.class)
                    .setRuntimeInit()
                    .unremovable()
                    // Pass runtime configuration to ensure initialization order
                    .supplier(minioRecorder.minioClientSupplier(minioClientName, miniosRuntimeConfiguration));
            if (MiniosBuildTimeConfiguration.isDefault(minioClientName)) {
                configurator.addQualifier(DotNames.DEFAULT);
            } else {
                configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", minioClientName).done();
                configurator.addQualifier().annotation(MinioQualifier.class)
                        .addValue("value", minioClientName).done();
            }
            syntheticBeanBuildItemBuildProducer.produce(configurator.done());
        }
    }

    private List<String> getClasses(String packageName, String regexp) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            assert classLoader != null;
            String path = packageName.replace('.', '/');
            URI uri = Objects.requireNonNull(classLoader.getResource(path)).toURI();
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap())) {
                Path packagePath = fileSystem.getPath(path);
                List<Path> dirs = Files.walk(packagePath, 1).collect(Collectors.toList());
                return dirs.stream().map(item -> findClasses(item, packageName, regexp))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String findClasses(Path path, String packageName, String regexp) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".class")) {
            String className = fileName.substring(0, fileName.length() - 6);
            if (regexp == null || className.matches(regexp)) {
                return packageName + '.' + className;
            }
        }
        return null;
    }
}
