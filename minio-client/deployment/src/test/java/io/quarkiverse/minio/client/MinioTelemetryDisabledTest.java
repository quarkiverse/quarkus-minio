package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class MinioTelemetryDisabledTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-valid.properties")
            .overrideConfigKey("quarkus.minio.telemetry.enabled", "false")
            .overrideConfigKey("quarkus.otel.traces.exporter", "none");

    @Inject
    Instance<TelemetryHttpClientCustomizer> telemetryCustomizer;

    @Inject
    OptionalHttpClientProducer httpClientProducer;

    @Test
    void shouldNotRegisterTelemetryWhenDisabled() {
        assertThat(telemetryCustomizer.isResolvable()).isFalse();
        assertThat(httpClientProducer.apply(MiniosBuildTimeConfiguration.DEFAULT_MINIOCLIENT_NAME)).isEmpty();
    }
}
