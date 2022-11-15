package io.quarkiverse.minio.client;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusUnitTest;

class MinioAllowEmptyAndInvalidUrlTest {

    @Inject
    Instance<MinioClient> minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-allow-empty-and-invalid-url.properties");

    @Test
    @Disabled
    public void invalidUrlThrowsException() {
        //Not validating other configuration keys as quarkus already does it for us.
        // toString method only here to trigger client instanciation
        Assertions.assertThatThrownBy(() -> minioClient.get().toString())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageStartingWith("\"quarkus.minio.url\" is mandatory");
    }
}
