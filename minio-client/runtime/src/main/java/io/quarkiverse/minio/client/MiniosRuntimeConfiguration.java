package io.quarkiverse.minio.client;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "minio", phase = ConfigPhase.RUN_TIME)
public class MiniosRuntimeConfiguration {

    /**
     * If value is `true` (default) and the `io.quarkus.quarkus-micrometer` is present in the class path,
     * then the minio client will produce metrics.
     *
     * Only true for clients produced by the extension
     *
     * @asciidoclet
     */
    @ConfigItem(defaultValue = "true")
    public boolean produceMetrics;

    /**
     * If minio clients are to produce metrics, then the uri tag will have a max of 100 values
     */
    @ConfigItem(defaultValue = "100")
    public Integer maximumAllowedTag;

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
}
