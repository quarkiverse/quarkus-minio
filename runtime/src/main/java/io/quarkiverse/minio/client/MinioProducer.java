package io.quarkiverse.minio.client;

import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.minio.MinioClient;
import io.quarkus.runtime.configuration.ConfigurationException;
import okhttp3.OkHttpClient;

@Singleton
public class MinioProducer {

    @Inject
    MinioConfiguration configuration;

    @Inject
    Optional<OkHttpClient> httpClient;

    private static final Predicate<String> IS_NOT_VALID_MINIO_URL = value -> !(value.startsWith("http://")
            || value.startsWith("https://"));

    @Produces
    @Dependent
    public MinioClient produceMinioClient() {
        if (configuration.returnEmptyClient()) {
            return null;
        }
        verifyUrl();
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(configuration.getUrl())
                .credentials(configuration.getAccessKey(), configuration.getSecretKey());
        configuration.region.ifPresent(builder::region);
        httpClient.ifPresent(builder::httpClient);
        return builder.build();
    }

    private void verifyUrl() {
        //Not validating other configuration keys as quarkus already does it for us.
        if (IS_NOT_VALID_MINIO_URL.test(configuration.getUrl())) {
            throw new ConfigurationException("\"quarkus.minio.url\" is mandatory and must be a valid url");
        }
    }
}
