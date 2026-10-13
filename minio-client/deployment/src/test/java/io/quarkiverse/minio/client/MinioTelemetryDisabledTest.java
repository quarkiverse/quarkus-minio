package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

class MinioTelemetryDisabledTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .withConfigurationResource("application-valid.properties")
            .overrideConfigKey("quarkus.minio.telemetry.enabled", "false")
            .overrideConfigKey("quarkus.otel.traces.exporter", "none");

    @Inject
    Instance<TelemetryHttpClientCustomizer> telemetryCustomizer;

    @Inject
    OptionalHttpClientProducer httpClientProducer;

    @Test
    void shouldNotApplyTelemetryWhenDisabled() {
        assertThat(telemetryCustomizer.get().isEnabled()).isFalse();
        assertThat(httpClientProducer.apply(MiniosBuildTimeConfiguration.DEFAULT_MINIOCLIENT_NAME)).isEmpty();
    }
}
