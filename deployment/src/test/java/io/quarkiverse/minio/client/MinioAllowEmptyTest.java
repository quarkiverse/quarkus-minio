package io.quarkiverse.minio.client;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.test.QuarkusUnitTest;

class MinioAllowEmptyTest {

    @Inject
    Instance<MinioClient> minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-allow-empty.properties");

    @Test
    public void instanceShouldNotBeProvided() {
        Assertions.assertThatThrownBy(() -> minioClient.get())
                .isInstanceOf(UnsatisfiedResolutionException.class)
                .hasMessage(
                        "No bean found for required type [class io.minio.MinioClient] and qualifiers [[@javax.enterprise.inject.Default()]]");
    }
}
