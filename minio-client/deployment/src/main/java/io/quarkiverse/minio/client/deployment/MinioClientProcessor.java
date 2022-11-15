package io.quarkiverse.minio.client.deployment;

import java.util.Map;
import java.util.function.BooleanSupplier;

import jakarta.inject.Singleton;

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
import io.quarkus.deployment.builditem.FeatureBuildItem;

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
}
