package io.quarkiverse.minio.client;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusUnitTest;

class NamedMinioInvalidUrlTest {

    @Inject
    @MinioQualifier("acme")
    Instance<MinioClient> minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-named-invalid-url.properties");

    @Test
    public void invalidUrlThrowsException() {
        Assertions.assertThatThrownBy(() -> minioClient.get())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageStartingWith("\"quarkus.minio.acme.url\" is mandatory");
    }
}
