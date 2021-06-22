package io.quarkiverse.minio.client;

import javax.enterprise.inject.Instance;
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
    public void testDefaultDataSourceInjection() {
        MinioClient mc = minioClient.get();
        Assertions.assertThat(mc == null);
    }
}
