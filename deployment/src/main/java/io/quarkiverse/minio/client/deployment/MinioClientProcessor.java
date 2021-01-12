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
import java.util.Objects;
import java.util.stream.Collectors;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.minio.BaseArgs;
import io.quarkiverse.minio.client.MinioProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

class MinioClientProcessor {

    private static final String FEATURE = "minio-client";
    private static final Logger LOGGER = Logger.getLogger(MinioClientProcessor.class.getName());

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

    @BuildStep
    AdditionalBeanBuildItem produce() {
        return AdditionalBeanBuildItem.unremovableOf(MinioProducer.class);
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
