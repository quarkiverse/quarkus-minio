package io.quarkiverse.minio.client;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "minio", phase = ConfigPhase.RUN_TIME)
public class MiniosRuntimeConfiguration {

    /**
     * If value is `true` (default) and the `io.quarkus.quarkus-micrometer` is present in the class path,
     * then the minio client will produce metrics.
     *
     * Value is set for minio clients produced.
     *
     * @asciidoclet
     */
    @ConfigItem
    public Optional<Boolean> produceMetrics;

    /**
     * The default client
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public MinioRuntimeConfiguration minio;

    /**
     * Additional named client
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, MinioRuntimeConfiguration> namedMinioClients;

    public MinioRuntimeConfiguration minio() {
        return minio;
    }

    public Map<String, MinioRuntimeConfiguration> namedMinioClients() {
        return namedMinioClients;
    }

    boolean produceMetrics() {
        return produceMetrics.orElse(true);
    }
}
