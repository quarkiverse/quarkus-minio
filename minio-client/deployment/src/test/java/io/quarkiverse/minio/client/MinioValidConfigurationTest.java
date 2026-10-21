package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.test.QuarkusExtensionTest;

class MinioValidConfigurationTest {

    @Inject
    MinioClient minioClient;

    @Inject
    OptionalHttpClientProducer okHttpClientOptional;

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .withConfigurationResource("application-valid-deprecated.properties")
            .overrideConfigKey("quarkus.minio.telemetry.enabled", "false");

    @Test
    public void testDefaultMinioClientInjection() {
        assertThatCode(() -> minioClient.toString()).doesNotThrowAnyException();
    }

    @Test
    public void testNoOkHttpClientProduced() {
        assertThat(okHttpClientOptional.apply("test")).isEmpty();
    }
}
