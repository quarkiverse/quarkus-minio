package io.quarkiverse.minio.client;

import okhttp3.OkHttpClient;

/**
 * Customizes the {@link OkHttpClient} used by the minio clients produced by the extension.
 * <p>
 * All enabled customizers are applied to a single {@link OkHttpClient.Builder} per minio client, so that features
 * such as metrics and telemetry can be combined.
 */
public interface MinioHttpClientCustomizer {

    /**
     * @return {@code true} if this customizer should be applied
     */
    default boolean isEnabled() {
        return true;
    }

    void customize(String minioClientName, OkHttpClient.Builder builder);
}
