package io.quarkiverse.minio.admin.client.deployment;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Singleton;

import io.minio.admin.MinioAdminClient;
import io.quarkiverse.minio.admin.runtime.MinioAdminClients;
import io.quarkiverse.minio.admin.runtime.MinioRecorder;
import io.quarkiverse.minio.client.*;
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

public class MinioAdminClientProcessor {
    private static final String FEATURE = "minio-admin-client";

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
            //No minio Admin client needed
            return;
        }
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(MinioAdminClients.class)
                .setUnremovable()
                .setDefaultScope(DotNames.SINGLETON).build());
        for (Map.Entry<String, MinioBuildTimeConfiguration> entry : miniosBuildTimeConfiguration.getMinioClients().entrySet()) {
            var minioAdminClientName = entry.getKey();
            syntheticBeanBuildItemBuildProducer.produce(
                    createMinioBeanBuildItem(minioAdminClientName,
                            // Pass runtime configuration to ensure initialization order
                            minioRecorder.minioAdminClientSupplier(minioAdminClientName)));

        }

        // add default bean based on whether or not micrometer is enabled
        if (metricsCapability.map(m -> m.metricsSupported(MetricsFactory.MICROMETER)).orElse(false)) {
            // we use the class name to not import any micrometer-related dependencies to prevent activation
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(WithMetricsHttpClientProducer.class));
        } else {
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(EmptyHttpClientProducer.class));
        }
    }

    private static SyntheticBeanBuildItem createMinioBeanBuildItem(String minioAdminClientName,
            Supplier<MinioAdminClient> clientSupplier) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(MinioAdminClient.class)
                .scope(Singleton.class)
                .setRuntimeInit()
                .supplier(clientSupplier);

        if (MiniosBuildTimeConfiguration.isDefault(minioAdminClientName)) {
            configurator.addQualifier(DotNames.DEFAULT);
        } else {
            configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", minioAdminClientName).done();
            configurator.addQualifier().annotation(MinioQualifier.class)
                    .addValue("value", minioAdminClientName).done();
        }

        return configurator.done();
    }
}
