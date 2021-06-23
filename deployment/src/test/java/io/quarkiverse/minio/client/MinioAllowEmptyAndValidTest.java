package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThatCode;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.test.QuarkusUnitTest;

class MinioAllowEmptyAndValidTest {

    @Inject
    MinioClient minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-allow-empty-and-valid.properties");

    @Test
    public void testDefaultDataSourceInjection() {
        assertThatCode(() -> minioClient.toString()).doesNotThrowAnyException();
    }
}
