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
    Optional<String> url = Optional.empty();

    /**
     * The minio server access key
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> accessKey = Optional.empty();

    /**
     * The minio server secret key
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> secretKey = Optional.empty();

    /**
     * An optional bucket region
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<String> region = Optional.empty();

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
