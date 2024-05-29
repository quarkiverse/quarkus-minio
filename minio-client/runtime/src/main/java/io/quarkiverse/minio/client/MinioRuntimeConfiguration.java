package io.quarkiverse.minio.client;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class MinioRuntimeConfiguration {

    /**
     * The minio server URL.
     * The url _may_ contains the port, though it's not recommended. If a specific port is needed, `quakus.minio.port` is a
     * better fit.
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

    /**
     * An optional port number.
     * Specifically usefull if you want to access bucket that doesn't use standard port (i.e. *80* for HTTP and *443* for HTTPS)
     *
     * @asciidoclet
     */
    Optional<Integer> port = Optional.empty();

    /**
     * An optional boolean to enable secure connection.
     * Defaults to `true`
     *
     * @asciidoclet
     */
    @ConfigItem(defaultValue = "true")
    boolean secure;

    public String getUrl() {
        return url.orElse("");
    }

    String getAccessKey() {
        return accessKey.orElse("");
    }

    String getSecretKey() {
        return secretKey.orElse("");
    }

    Optional<Integer> getPort() {
        return port;
    }

    Boolean isTls() {
        return secure;
    }

}
