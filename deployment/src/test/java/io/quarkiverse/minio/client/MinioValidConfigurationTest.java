package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.test.QuarkusUnitTest;
import okhttp3.OkHttpClient;

class MinioValidConfigurationTest {

    @Inject
    MinioClient minioClient;

    @Inject
    Optional<OkHttpClient> okHttpClientOptional;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-valid.properties");

    @Test
    public void testDefaultMinioClientInjection() {
        assertThatCode(() -> minioClient.toString()).doesNotThrowAnyException();
    }

    @Test
    public void testNoOkHttpCilentProduced() {
        assertThat(okHttpClientOptional).isEmpty();
    }
}
