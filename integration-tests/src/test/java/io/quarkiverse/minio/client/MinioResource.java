package io.quarkiverse.minio.client;

import java.util.Map;
import java.util.Optional;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class MinioResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private Optional<String> containerNetworkId;
    private GenericContainer<?> minioContainer;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        containerNetworkId = context.containerNetworkId();
    }

    @Override
    public Map<String, String> start() {
        minioContainer = new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2020-11-10T21-02-24Z"))
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", "minioaccess")
                .withEnv("MINIO_SECRET_KEY", "miniosecret")
                .withCommand("server /data")
                .waitingFor(new HttpWaitStrategy().forPort(9000).forPath("/minio/health/live"));
        containerNetworkId.ifPresent(minioContainer::withNetworkMode);
        minioContainer.start();
        return Map.of("quarkus.minio.access-key", "minioaccess",
                "quarkus.minio.secret-key", "miniosecret",
                "quarkus.minio.url",
                String.format("http://%s:%s", minioContainer.getHost(), minioContainer.getMappedPort(9000)));
    }

    @Override
    public void stop() {
        minioContainer.stop();
    }
}
