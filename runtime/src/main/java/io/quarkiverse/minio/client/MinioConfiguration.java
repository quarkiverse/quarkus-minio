package io.quarkiverse.minio.client;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public class MinioConfiguration {

    /**
     * The minio server URL.
     * 
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
     * If value is false (default) or some of other properties is present, then producer behaves as this option is not set.
     * If value is true and and all other configuration options are empty, producer returns null as a client.
     *
     * @asciidoclet
     */
    @ConfigItem
    Optional<Boolean> allowEmpty;

    public String getUrl() {
        return url.orElse("");
    }

    String getAccessKey() {
        return accessKey.orElse("");
    }

    String getSecretKey() {
        return secretKey.orElse("");
    }

    boolean returnEmptyClient() {
        return allowEmpty.orElse(false) && !url.isPresent() && !accessKey.isPresent() && !secretKey.isPresent();
    }
}
