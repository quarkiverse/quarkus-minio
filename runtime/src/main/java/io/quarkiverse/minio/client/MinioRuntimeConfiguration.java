package io.quarkiverse.minio.client;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class MinioRuntimeConfiguration {

    /**
     * The minio server URL.
     * <p>
     * [NOTE]
     * ====
     * Value must start with `http://` or `https://`
     * ====
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> url;

    /**
     * The minio server access key
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> accessKey;

    /**
     * The minio server secret key
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> secretKey;

    /**
     * An optional bucket region
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> region;

    public String getUrl() {
        return url.orElse("");
    }

    String getAccessKey() {
        return accessKey.orElse("");
    }

    String getSecretKey() {
        return secretKey.orElse("");
    }
}
