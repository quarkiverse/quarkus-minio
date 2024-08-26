package io.quarkiverse.minio.client.deployment.devservices;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.minio")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface MinioBuildTimeConfig {

    /**
     * Configuration for DevServices. DevServices allows Quarkus to automatically start Minio in dev and test mode.
     */
    MinioDevServicesBuildTimeConfig devservices();
}
