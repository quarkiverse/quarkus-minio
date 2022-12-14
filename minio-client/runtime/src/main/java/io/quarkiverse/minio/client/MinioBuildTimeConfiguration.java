package io.quarkiverse.minio.client;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class MinioBuildTimeConfiguration {

    /**
     * If we create minio client for this bucket
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;
}
