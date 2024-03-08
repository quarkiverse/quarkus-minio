package io.quarkiverse.minio.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import jakarta.inject.Singleton;

import io.minio.MinioClient;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.configuration.ConfigurationException;

@Singleton
public class MinioClients {

    private final MiniosBuildTimeConfiguration miniosBuildTimeConfiguration;

    private final MiniosRuntimeConfiguration miniosRuntimeConfiguration;

    private final ConcurrentMap<String, MinioClient> minioClients = new ConcurrentHashMap<>();

    private final OptionalHttpClientProducer httpClientProducer;

    private static final Predicate<String> IS_MANDATORY_URL = value -> value == null || value.isBlank();

    public MinioClients(
            MiniosBuildTimeConfiguration miniosBuildTimeConfiguration,
            MiniosRuntimeConfiguration miniosRuntimeConfiguration,
            OptionalHttpClientProducer httpClientProducer) {
        this.miniosBuildTimeConfiguration = miniosBuildTimeConfiguration;
        this.miniosRuntimeConfiguration = miniosRuntimeConfiguration;
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

    public MinioClient getMinioClient(String minioClientName) {
        return minioClients.computeIfAbsent(minioClientName, this::createMinioClient);
    }

    public MinioClient createMinioClient(String minioClientName) {
        if (!miniosBuildTimeConfiguration.getMinioClients().containsKey(minioClientName)) {
            throw new IllegalArgumentException("No Minioclient named '" + minioClientName + "' exists");
        }

        MinioRuntimeConfiguration configuration = getConfiguration(minioClientName);

        if (IS_MANDATORY_URL.test(configuration.getUrl())) {
            String errorMessage;
            if (MiniosBuildTimeConfiguration.isDefault(minioClientName)) {
                errorMessage = "\"quarkus.minio.url\" is mandatory and must be a valid url";
            } else {
                errorMessage = "\"quarkus.minio." + minioClientName + ".url\" is mandatory";
            }
            throw new ConfigurationException(errorMessage);
        }

        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(configuration.getUrl(), configuration.getPort(), configuration.isTls())
                .credentials(configuration.getAccessKey(), configuration.getSecretKey());
        configuration.region.ifPresent(builder::region);
        if (miniosRuntimeConfiguration.produceMetrics) {
            httpClientProducer.apply(minioClientName).ifPresent(builder::httpClient);
        }
        return builder.build();
    }

    private MinioRuntimeConfiguration getConfiguration(String minioClientName) {
        if (MiniosBuildTimeConfiguration.isDefault(minioClientName)) {
            return miniosRuntimeConfiguration.minio();
        }
        return miniosRuntimeConfiguration.namedMinioClients().getOrDefault(minioClientName,
                new MinioRuntimeConfiguration());
    }
}
