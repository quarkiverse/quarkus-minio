package io.quarkiverse.minio.client;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.minio")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface MiniosRuntimeConfiguration {

    /**
     * If value is `true` (default) and the `io.quarkus.quarkus-micrometer` is present in the class path,
     * then the minio client will produce metrics.
     *
     * Only true for clients produced by the extension
     *
     * @asciidoclet
     */
    @WithDefault("true")
    boolean produceMetrics();

    /**
     * If minio clients are to produce metrics, then the uri tag will have a max of 100 values
     */
    @WithDefault("100")
    Integer maximumAllowedTag();

    /**
     * The default client
     */
    @WithParentName()
    Optional<MinioRuntimeConfiguration> minio();

    /**
     * Additional named client
     */
    @WithParentName()
    Map<String, MinioRuntimeConfiguration> namedMinioClients();
}
