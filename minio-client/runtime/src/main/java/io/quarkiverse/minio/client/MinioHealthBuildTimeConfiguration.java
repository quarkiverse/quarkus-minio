package io.quarkiverse.minio.client;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface MinioHealthBuildTimeConfiguration {

    /**
     * If value is `true` (default) and the `io.quarkus:quarkus-smallrye-health` extension is present in the class path,
     * then a readiness health check is published for the minio clients produced by the extension.
     *
     * @asciidoclet
     */
    @WithDefault("true")
    boolean enabled();
}
