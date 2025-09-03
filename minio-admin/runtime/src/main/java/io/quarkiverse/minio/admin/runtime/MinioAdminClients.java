package io.quarkiverse.minio.admin.runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import jakarta.inject.Singleton;

import io.minio.admin.MinioAdminClient;
import io.minio.http.HttpUtils;
import io.quarkiverse.minio.client.MinioRuntimeConfiguration;
import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkiverse.minio.client.MiniosConfiguration;
import io.quarkiverse.minio.client.OptionalHttpClientProducer;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.configuration.ConfigurationException;

@Singleton
public class MinioAdminClients {

    private final MiniosBuildTimeConfiguration miniosBuildTimeConfiguration;

    private final MiniosConfiguration miniosConfiguration;

    private final ConcurrentMap<String, MinioAdminClient> minioAdminClients = new ConcurrentHashMap<>();
    private final OptionalHttpClientProducer httpClientProducer;
    private static final Predicate<String> IS_HOST_SET = value -> value == null || value.isBlank();

    public MinioAdminClients(MiniosBuildTimeConfiguration miniosBuildTimeConfiguration,
            MiniosConfiguration miniosConfiguration, OptionalHttpClientProducer httpClientProducer) {
        this.miniosBuildTimeConfiguration = miniosBuildTimeConfiguration;
        this.miniosConfiguration = miniosConfiguration;
        this.httpClientProducer = httpClientProducer;
    }

    /**
     * Meant to be used from recorders that create synthetic beans that need access to {@code MinioAdminClient}.
     * In such using {@code Arc.container.instance(MinioAdminClient.class)} is not possible because
     * {@code MinioAdminClient} is itself a synthetic bean.
     * <p>
     * This method relies on the fact that {@code MinioAdminClient} should - given the same input -
     * always return the same {@code MinioAdminClient} no matter how many times it is invoked
     * (which makes sense because {@code MinioAdminClient} is a {@code Singleton} bean).
     * <p>
     * This method is thread-safe
     */
    public static MinioAdminClient fromName(String minioClientName) {
        return Arc.container().instance(MinioAdminClients.class).get()
                .getMinioAdminClient(minioClientName);
    }

    public MinioAdminClient getMinioAdminClient(String minioClientName) {
        return minioAdminClients.computeIfAbsent(minioClientName, this::createMinioAdminClient);
    }

    public MinioAdminClient createMinioAdminClient(String minioClientName) {
        MinioRuntimeConfiguration configuration = getAndVerifyConfiguration(minioClientName);

        MinioAdminClient.Builder builder = MinioAdminClient.builder();
        configuration.port()
                .ifPresentOrElse(
                        port -> builder.endpoint(configuration.host(), port, configuration.secure()),
                        () -> builder.endpoint(
                                HttpUtils.getBaseUrl(configuration.host()).newBuilder()
                                        .scheme(configuration.secure() ? "https" : "http").build()));

        builder.credentials(configuration.accessKey(), configuration.secretKey());
        configuration.region().ifPresent(builder::region);
        if (miniosConfiguration.produceMetrics()) {
            httpClientProducer.apply(minioClientName).ifPresent(builder::httpClient);
        }
        return builder.build();
    }

    private MinioRuntimeConfiguration getAndVerifyConfiguration(String minioAdminClientName) {
        if (!miniosBuildTimeConfiguration.getMinioClients().containsKey(minioAdminClientName)) {
            throw new IllegalArgumentException("No MinioAdminClient named '" + minioAdminClientName + "' exists");
        }

        MinioRuntimeConfiguration configuration;
        if (MiniosBuildTimeConfiguration.isDefault(minioAdminClientName)) {
            configuration = miniosConfiguration.minio().get();
        } else {
            configuration = miniosConfiguration.namedMinioClients().get(minioAdminClientName);
        }

        if (configuration == null || IS_HOST_SET.test(configuration.host())) {
            String errorMessage;
            if (MiniosBuildTimeConfiguration.isDefault(minioAdminClientName)) {
                errorMessage = "\"quarkus.minio.host\" is mandatory.";
            } else {
                errorMessage = "\"quarkus.minio." + minioAdminClientName + ".host\" is mandatory.";
            }
            throw new ConfigurationException(errorMessage);
        }

        return configuration;
    }
}
