package io.quarkiverse.minio.client;

import java.util.function.Predicate;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.minio.MinioClient;
import io.quarkus.runtime.configuration.ConfigurationException;

@Singleton
public class MinioProducer {

    @Inject
    MinioConfiguration configuration;

    private static final Predicate<String> IS_NOT_VALID_MINIO_URL = value -> !(value.startsWith("http://")
            || value.startsWith("https://"));

    @Produces
    @Singleton
    public MinioClient produceMinioClient() {
        verifyUrl();
        return MinioClient.builder()
                .endpoint(configuration.getUrl())
                .credentials(configuration.getAccessKey(), configuration.getSecretKey())
                .build();
    }

    private void verifyUrl() {
        //Not validating other configuration keys as quarkus already does it for us.
        if (IS_NOT_VALID_MINIO_URL.test(configuration.getUrl())) {
            throw new ConfigurationException("\"quarkus.minio.url\" is mandatory and must be a valid url");
        }
    }
}
