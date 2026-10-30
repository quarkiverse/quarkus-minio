package io.quarkiverse.minio.client.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.minio.MinioClient;
import io.quarkiverse.minio.client.MinioClients;
import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;

/**
 * Readiness health check for the minio clients produced by the extension.
 * <p>
 * Each configured client is probed with a {@code listBuckets} call, which is a lightweight, idempotent
 * operation supported by every S3 compatible server.
 */
@Readiness
@ApplicationScoped
public class MinioHealthCheck implements HealthCheck {

    @Inject
    MiniosBuildTimeConfiguration miniosBuildTimeConfiguration;

    @Inject
    MinioClients minioClients;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Minio health check").up();
        for (String minioClientName : miniosBuildTimeConfiguration.getMinioClients().keySet()) {
            String name = MiniosBuildTimeConfiguration.isDefault(minioClientName) ? "default" : minioClientName;
            try {
                MinioClient minioClient = minioClients.getMinioClient(minioClientName);
                minioClient.listBuckets();
                builder.withData(name, "OK");
            } catch (Exception e) {
                builder.down().withData(name, "KO, reason: " + e.getMessage());
            }
        }
        return builder.build();
    }
}
