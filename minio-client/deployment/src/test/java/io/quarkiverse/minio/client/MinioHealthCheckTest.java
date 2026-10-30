package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.sun.net.httpserver.HttpServer;

import io.quarkus.test.QuarkusExtensionTest;
import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class MinioHealthCheckTest {

    private static final String LIST_BUCKETS_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
            + "<Owner><ID>minio</ID><DisplayName>minio</DisplayName></Owner>"
            + "<Buckets></Buckets></ListAllMyBucketsResult>";

    static final HttpServer SERVER = startServer();

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .overrideConfigKey("quarkus.minio.devservices.enabled", "false")
            .overrideConfigKey("quarkus.minio.host", "localhost")
            .overrideConfigKey("quarkus.minio.port", String.valueOf(SERVER.getAddress().getPort()))
            .overrideConfigKey("quarkus.minio.secure", "false")
            .overrideConfigKey("quarkus.minio.access-key", "access-key")
            .overrideConfigKey("quarkus.minio.secret-key", "secret-key")
            .overrideConfigKey("quarkus.minio.region", "us-east-1")
            .overrideConfigKey("quarkus.minio.telemetry.enabled", "false");

    @Inject
    SmallRyeHealthReporter reporter;

    @org.junit.jupiter.api.AfterAll
    static void stopServer() {
        SERVER.stop(0);
    }

    @Test
    void shouldReportReadinessUp() {
        SmallRyeHealth health = reporter.getReadiness();
        assertThat(health.isDown()).isFalse();

        JsonObject check = findCheck(health, "Minio health check");
        assertThat(check).isNotNull();
        assertThat(check.getString("status")).isEqualTo("UP");
        assertThat(check.getJsonObject("data").getString("default")).isEqualTo("OK");
    }

    private static JsonObject findCheck(SmallRyeHealth health, String name) {
        JsonArray checks = new JsonObject(health.getPayload().toString()).getJsonArray("checks");
        for (int i = 0; i < checks.size(); i++) {
            JsonObject check = checks.getJsonObject(i);
            if (name.equals(check.getString("name"))) {
                return check;
            }
        }
        return null;
    }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                byte[] body = LIST_BUCKETS_RESPONSE.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/xml");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
