package io.quarkiverse.minio.client;

import jakarta.inject.Singleton;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.internal.OnlyOnceLoggingDenyMeterFilter;
import okhttp3.OkHttpClient;

/**
 * Adds Micrometer metrics to the minio http client.
 * Only registered when the `quarkus-micrometer` extension is present.
 */
@Singleton
public class MetricsHttpClientCustomizer implements MinioHttpClientCustomizer {

    private final MeterRegistry meterRegistry;

    private final MiniosConfiguration configuration;

    public MetricsHttpClientCustomizer(MeterRegistry meterRegistry, MiniosConfiguration configuration) {
        this.meterRegistry = meterRegistry;
        this.configuration = configuration;
    }

    @Override
    public boolean isEnabled() {
        return configuration.produceMetrics();
    }

    @Override
    public void customize(String minioClientName, OkHttpClient.Builder builder) {
        var metricKey = MiniosBuildTimeConfiguration.isDefault(minioClientName) ? "minio.client"
                : "minio." + minioClientName + ".client";
        meterRegistry.config().meterFilter(maximumAllowedTag(metricKey));
        builder.eventListener(
                OkHttpMetricsEventListener.builder(meterRegistry, metricKey)
                        .uriMapper(request -> String.join("/", request.url().pathSegments()))
                        .build());
    }

    private MeterFilter maximumAllowedTag(String metricName) {
        MeterFilter denyFilter = new OnlyOnceLoggingDenyMeterFilter(() -> String
                .format("Reached the maximum number (%s) of URI tags for '%s'. Are you using path parameters?",
                        configuration.maximumAllowedTag(), metricName));

        return MeterFilter.maximumAllowableTags(metricName, "uri", configuration.maximumAllowedTag(), denyFilter);
    }
}
