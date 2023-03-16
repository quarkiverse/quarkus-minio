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

class MissingNamedRuntimeConfigTest {

    @Inject
    @MinioQualifier("acme")
    Instance<MinioClient> minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("missing-named-runtime-conf.properties");

    @Test
    public void unexpected() {
        Assertions.assertThatThrownBy(() -> minioClient.get())
                .isInstanceOf(CreationException.class)
                .hasMessageContaining("\"quarkus.minio.acme.url\" is mandatory")
                .hasCauseInstanceOf(ConfigurationException.class);
    }
}
