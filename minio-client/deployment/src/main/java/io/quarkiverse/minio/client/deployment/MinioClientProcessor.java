package io.quarkiverse.minio.client.deployment;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Singleton;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.quarkiverse.minio.client.EmptyHttpClientProducer;
import io.quarkiverse.minio.client.MinioBuildTimeConfiguration;
import io.quarkiverse.minio.client.MinioClients;
import io.quarkiverse.minio.client.MinioQualifier;
import io.quarkiverse.minio.client.MinioRecorder;
import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkiverse.minio.client.WithMetricsHttpClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;

public class MinioClientProcessor {

    public static final String FEATURE = "minio-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void produce(MiniosBuildTimeConfiguration miniosBuildTimeConfiguration,
            MinioRecorder minioRecorder,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanBuildItemBuildProducer,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            Optional<MetricsCapabilityBuildItem> metricsCapability) {
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

        // add default bean based on whether or not micrometer is enabled
        if (metricsCapability.map(m -> m.metricsSupported(MetricsFactory.MICROMETER)).orElse(false)) {
            // we use the class name to not import any micrometer-related dependencies to prevent activation
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(WithMetricsHttpClientProducer.class));
        } else {
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(EmptyHttpClientProducer.class));
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
