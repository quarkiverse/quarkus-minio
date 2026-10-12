package io.quarkiverse.minio.client;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface MinioTelemetryBuildTimeConfiguration {

    /**
     * If value is `true` (default) and the `io.quarkus:quarkus-opentelemetry` extension is present in the class path,
     * then the minio clients will produce an OpenTelemetry `CLIENT` span for each request sent to the minio server.
     *
     * Only true for clients produced by the extension
     *
     * @asciidoclet
     */
    @WithDefault("true")
    boolean enabled();
}
