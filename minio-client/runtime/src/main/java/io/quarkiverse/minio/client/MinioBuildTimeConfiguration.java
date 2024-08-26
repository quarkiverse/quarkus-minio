package io.quarkiverse.minio.client;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface MinioBuildTimeConfiguration {

    /**
     * Should the extension provide a `MinioClient`.
     * If set to `false`, you will have to create the clients yourself,
     * but will still benefit the native compatibility work.
     */
    @WithDefault("true")
    boolean enabled();
}
