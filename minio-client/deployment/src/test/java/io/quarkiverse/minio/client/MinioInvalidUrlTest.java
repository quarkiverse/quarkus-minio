package io.quarkiverse.minio.client;

import jakarta.enterprise.inject.CreationException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusUnitTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinioInvalidUrlTest {

    @Inject
    Instance<MinioClient> minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-invalid-url.properties");

    @Test
    public void invalidUrlThrowsException() {
        //Not validating other configuration keys as quarkus already does it for us.
        // toString method only here to trigger client instanciation
        Assertions.assertThatThrownBy(() -> minioClient.get())
                .hasMessageContaining("\"quarkus.minio.url\" is mandatory")
                .hasCauseInstanceOf(ConfigurationException.class);
    }
}
