package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;
import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class MinioHealthCheckDisabledTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .withConfigurationResource("application-valid.properties")
            .overrideConfigKey("quarkus.minio.telemetry.enabled", "false")
            .overrideConfigKey("quarkus.minio.health.enabled", "false");

    @Inject
    SmallRyeHealthReporter reporter;

    @Test
    void shouldNotPublishHealthCheckWhenDisabled() {
        SmallRyeHealth health = reporter.getReadiness();
        JsonArray checks = new JsonObject(health.getPayload().toString()).getJsonArray("checks");
        boolean present = false;
        for (int i = 0; i < checks.size(); i++) {
            if ("Minio health check".equals(checks.getJsonObject(i).getString("name"))) {
                present = true;
            }
        }
        assertThat(present).isFalse();
    }
}
