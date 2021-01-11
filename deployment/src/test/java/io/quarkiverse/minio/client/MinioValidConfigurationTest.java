package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThatCode;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.test.QuarkusUnitTest;

class MinioValidConfigurationTest {

    @Inject
    MinioClient minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-valid.properties");

    @Test
    public void testDefaultDataSourceInjection() {
        assertThatCode(() -> minioClient.toString()).doesNotThrowAnyException();
    }
}
