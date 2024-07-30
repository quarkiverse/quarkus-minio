package io.quarkiverse.minio.client.deployment;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import io.quarkiverse.minio.client.*;
import jakarta.inject.Singleton;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
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

    @BuildStep(onlyIfNot = {MetricsEnabled.class})
    void ifMetricsAreDisabled(
            BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemBuildProducer) {
        additionalBeanBuildItemBuildProducer
                .produce(AdditionalBeanBuildItem.unremovableOf(EmptyHttpClientProducer.class));
    }

    @BuildStep(onlyIf = {MetricsEnabled.class})
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
            syntheticBeanBuildItemBuildProducer.produce(
                    createMinioBeanBuildItem(minioClientName,
                            MinioClient.class,
                            // Pass runtime configuration to ensure initialization order
                            minioRecorder.minioClientSupplier(minioClientName, miniosRuntimeConfiguration)));

            syntheticBeanBuildItemBuildProducer.produce(
                    createMinioBeanBuildItem(minioClientName,
                            MinioAsyncClient.class,
                            // Pass runtime configuration to ensure initialization order
                            minioRecorder.minioAsyncClientSupplier(minioClientName, miniosRuntimeConfiguration)));

            syntheticBeanBuildItemBuildProducer.produce(
                    createMinioBeanBuildItem(minioClientName,
                            MutinyMinioClient.class,
                            // Pass runtime configuration to ensure initialization order
                            minioRecorder.mutinyMinioClientSupplier(minioClientName, miniosRuntimeConfiguration)));
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
