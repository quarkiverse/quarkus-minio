package io.quarkiverse.minio.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import jakarta.inject.Singleton;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.minio.http.HttpUtils;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.configuration.ConfigurationException;

@Singleton
public class MinioClients {

    private final MiniosBuildTimeConfiguration miniosBuildTimeConfiguration;

    private final MiniosConfiguration miniosConfiguration;

    private final ConcurrentMap<String, MinioClient> minioClients = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, MinioAsyncClient> minioAsyncClients = new ConcurrentHashMap<>();

    private final OptionalHttpClientProducer httpClientProducer;

    private static final Predicate<String> IS_HOST_SET = value -> value == null || value.isBlank();

    public MinioClients(
            MiniosBuildTimeConfiguration miniosBuildTimeConfiguration,
            MiniosConfiguration miniosConfiguration,
            OptionalHttpClientProducer httpClientProducer) {
        this.miniosBuildTimeConfiguration = miniosBuildTimeConfiguration;
        this.miniosConfiguration = miniosConfiguration;
        this.httpClientProducer = httpClientProducer;
    }

    /**
     * Meant to be used from recorders that create synthetic beans that need access to {@code MinioClient}.
     * In such using {@code Arc.container.instance(MinioClient.class)} is not possible because
     * {@code MinioClient} is itself a synthetic bean.
     * <p>
     * This method relies on the fact that {@code MinioClients} should - given the same input -
     * always return the same {@code MinioClient} no matter how many times it is invoked
     * (which makes sense because {@code MinioClient} is a {@code Singleton} bean).
     * <p>
     * This method is thread-safe
     */
    public static MinioClient fromName(String minioClientName) {
        return Arc.container().instance(MinioClients.class).get()
                .getMinioClient(minioClientName);
    }

    /**
     * Meant to be used from recorders that create synthetic beans that need access to {@code MinioAsyncClient}.
     * In such using {@code Arc.container.instance(MinioAsyncClient.class)} is not possible because
     * {@code MinioAsyncClient} is itself a synthetic bean.
     * <p>
     * This method relies on the fact that {@code MinioClients} should - given the same input -
     * always return the same {@code MinioAsyncClient} no matter how many times it is invoked
     * (which makes sense because {@code MinioAsyncClient} is a {@code Singleton} bean).
     * <p>
     * This method is thread-safe
     */
    public static MinioAsyncClient fromNameAsync(String minioClientName) {
        return Arc.container().instance(MinioClients.class).get()
                .getMinioAsyncClient(minioClientName);
    }

    public MinioClient getMinioClient(String minioClientName) {
        return minioClients.computeIfAbsent(minioClientName, this::createMinioClient);
    }

    public MinioAsyncClient getMinioAsyncClient(String minioClientName) {
        return minioAsyncClients.computeIfAbsent(minioClientName, this::createMinioAsyncClient);
    }

    public MinioClient createMinioClient(String minioClientName) {
        MinioRuntimeConfiguration configuration = getAndVerifyConfiguration(minioClientName);

        MinioClient.Builder builder = MinioClient.builder();
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

    public MinioAsyncClient createMinioAsyncClient(String minioClientName) {
        MinioRuntimeConfiguration configuration = getAndVerifyConfiguration(minioClientName);

        MinioAsyncClient.Builder builder = MinioAsyncClient.builder();
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

    private MinioRuntimeConfiguration getAndVerifyConfiguration(String minioClientName) {
        if (!miniosBuildTimeConfiguration.getMinioClients().containsKey(minioClientName)) {
            throw new IllegalArgumentException("No Minioclient named '" + minioClientName + "' exists");
        }

        MinioRuntimeConfiguration configuration;
        if (MiniosBuildTimeConfiguration.isDefault(minioClientName)) {
            configuration = miniosConfiguration.minio().get();
        } else {
            configuration = miniosConfiguration.namedMinioClients().get(minioClientName);
        }

        if (configuration == null || IS_HOST_SET.test(configuration.host())) {
            String errorMessage;
            if (MiniosBuildTimeConfiguration.isDefault(minioClientName)) {
                errorMessage = "\"quarkus.minio.host\" is mandatory.";
            } else {
                errorMessage = "\"quarkus.minio." + minioClientName + ".host\" is mandatory.";
            }
            throw new ConfigurationException(errorMessage);
        }

        return configuration;
    }
}
