package io.quarkiverse.minio.client;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class MinioRuntimeConfiguration {

    /**
     * The minio server URL.
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

    /**
     * An optional port number
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<Integer> port = Optional.empty();

    /**
     * An optional boolean to enable secure connection
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<Boolean> secure = Optional.empty();

    public String getUrl() {
        return url.orElse("");
    }

    String getAccessKey() {
        return accessKey.orElse("");
    }

    String getSecretKey() {
        return secretKey.orElse("");
    }

    Integer getPort() {
        return port.orElse(9000);
    }

    Boolean isTls() {
        return secure.orElse(true);
    }
}
