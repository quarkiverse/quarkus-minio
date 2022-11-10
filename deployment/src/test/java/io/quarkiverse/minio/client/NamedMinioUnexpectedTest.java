package io.quarkiverse.minio.client;

import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.minio.MinioClient;
import io.quarkus.test.QuarkusUnitTest;

class NamedMinioUnexpectedTest {

    @Inject
    @MinioQualifier("acme")
    MinioClient minioClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-valid.properties")
            .setExpectedException(UnsatisfiedResolutionException.class);

    @Test
    public void unexpected() {
        // Just trigger test
    }
}
