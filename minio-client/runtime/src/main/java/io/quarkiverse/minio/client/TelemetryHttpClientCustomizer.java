package io.quarkiverse.minio.client;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Singleton;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Adds OpenTelemetry tracing to the minio http client.
 * Only registered when the `quarkus-opentelemetry` extension is present, and only applied when
 * `quarkus.minio.telemetry.enabled` is `true`.
 */
@Singleton
public class TelemetryHttpClientCustomizer implements MinioHttpClientCustomizer {

    private final OpenTelemetry openTelemetry;

    private final MiniosBuildTimeConfiguration configuration;

    public TelemetryHttpClientCustomizer(OpenTelemetry openTelemetry, MiniosBuildTimeConfiguration configuration) {
        this.openTelemetry = openTelemetry;
        this.configuration = configuration;
    }

    @Override
    public boolean isEnabled() {
        return configuration.telemetry().enabled();
    }

    @Override
    public void customize(String minioClientName, OkHttpClient.Builder builder) {
        builder.addInterceptor(new TelemetryInterceptor(openTelemetry, minioClientName))
                // MinioAsyncClient runs calls on the OkHttp dispatcher threads, so the caller's context must be
                // propagated to them for the client spans to be attached to the current trace.
                .dispatcher(new Dispatcher(Context.taskWrapping(new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
                        runnable -> new Thread(runnable, "OkHttp Dispatcher")))));
    }
}
