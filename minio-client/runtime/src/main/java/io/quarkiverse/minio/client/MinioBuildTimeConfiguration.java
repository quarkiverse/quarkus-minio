package io.quarkiverse.minio.client;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class MinioBuildTimeConfiguration {

    /**
     * Should the extension provide a `MinioClient`.
     * If set to `false`, you will have to create the clients yourself,
     * but will still benefit the native compatibility work.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;
}
