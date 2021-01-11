package io.quarkiverse.minio.client;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

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
