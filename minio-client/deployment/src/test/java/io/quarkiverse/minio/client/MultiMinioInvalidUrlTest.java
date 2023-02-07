package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusUnitTest;

class MultiMinioInvalidUrlTest {

    @Inject
    Instance<MinioClient> minioClient;

    @Inject
    @Named("another")
    Instance<MinioClient> anotherValidMinioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-one-invalid-url-the-other-valid.properties");

    @Test
    public void oneInvalidUrlTheOtherValidThrowsException() {
        //Not validating other configuration keys as quarkus already does it for us.
        // toString method only here to trigger client instanciation
        assertThatThrownBy(() -> minioClient.get())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageStartingWith("\"quarkus.minio.url\" is mandatory");
        assertThatCode(() -> anotherValidMinioClient.toString()).doesNotThrowAnyException();
    }
}
