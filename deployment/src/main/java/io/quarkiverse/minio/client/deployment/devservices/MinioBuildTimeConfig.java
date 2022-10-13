package io.quarkiverse.minio.client.deployment.devservices;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "minio", phase = ConfigPhase.BUILD_TIME)
public class MinioBuildTimeConfig {

    /**
     * Configuration for DevServices. DevServices allows Quarkus to automatically start Minio in dev and test mode.
     */
    @ConfigItem
    public MinioDevServicesBuildTimeConfig devservices;
}
