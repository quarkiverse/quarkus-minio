package io.quarkiverse.minio.client.deployment;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Singleton;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.quarkiverse.minio.client.MetricsHttpClientCustomizer;
import io.quarkiverse.minio.client.MinioBuildTimeConfiguration;
import io.quarkiverse.minio.client.MinioClients;
import io.quarkiverse.minio.client.MinioHttpClientProducer;
import io.quarkiverse.minio.client.MinioQualifier;
import io.quarkiverse.minio.client.MinioRecorder;
import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkiverse.minio.client.TelemetryHttpClientCustomizer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class MinioClientProcessor {

    public static final String FEATURE = "minio-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    HealthBuildItem addHealthCheck(MiniosBuildTimeConfiguration miniosBuildTimeConfiguration) {
        return new HealthBuildItem("io.quarkiverse.minio.client.health.MinioHealthCheck",
                miniosBuildTimeConfiguration.health().enabled());
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void produce(MiniosBuildTimeConfiguration miniosBuildTimeConfiguration,
            MinioRecorder minioRecorder,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanBuildItemBuildProducer,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            Optional<MetricsCapabilityBuildItem> metricsCapability,
            Capabilities capabilities) {
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
            syntheticBeanBuildItemBuildProducer.produce(
                    createMinioBeanBuildItem(minioClientName,
                            MinioClient.class,
                            // Pass runtime configuration to ensure initialization order
                            minioRecorder.minioClientSupplier(minioClientName)));

            syntheticBeanBuildItemBuildProducer.produce(
                    createMinioBeanBuildItem(minioClientName,
                            MinioAsyncClient.class,
                            // Pass runtime configuration to ensure initialization order
                            minioRecorder.minioAsyncClientSupplier(minioClientName)));
        }

        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(MinioHttpClientProducer.class));
        if (metricsCapability.map(m -> m.metricsSupported(MetricsFactory.MICROMETER)).orElse(false)) {
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(MetricsHttpClientCustomizer.class));
        }
        if (capabilities.isPresent(Capability.OPENTELEMETRY_TRACER)) {
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(TelemetryHttpClientCustomizer.class));
        }
    }

    private static <T> SyntheticBeanBuildItem createMinioBeanBuildItem(String minioClientName, Class<T> clientClass,
            Supplier<T> clientSupplier) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(clientClass)
                .scope(Singleton.class)
                .setRuntimeInit()
                .supplier(clientSupplier);

        if (MiniosBuildTimeConfiguration.isDefault(minioClientName)) {
            configurator.addQualifier(DotNames.DEFAULT);
        } else {
            configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", minioClientName).done();
            configurator.addQualifier().annotation(MinioQualifier.class)
                    .addValue("value", minioClientName).done();
        }

        return configurator.done();
    }
}
