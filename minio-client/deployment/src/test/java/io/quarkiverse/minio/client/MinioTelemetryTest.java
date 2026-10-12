package io.quarkiverse.minio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;

import io.quarkus.test.QuarkusExtensionTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.sun.net.httpserver.HttpServer;

import io.minio.BucketExistsArgs;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.quarkus.test.QuarkusUnitTest;

class MinioTelemetryTest {

    static final HttpServer SERVER = startServer();

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .withApplicationRoot(jar -> jar.addClass(InMemorySpanExporterProducer.class))
            .overrideConfigKey("quarkus.minio.devservices.enabled", "false")
            .overrideConfigKey("quarkus.minio.host", "localhost")
            .overrideConfigKey("quarkus.minio.port", String.valueOf(SERVER.getAddress().getPort()))
            .overrideConfigKey("quarkus.minio.secure", "false")
            .overrideConfigKey("quarkus.minio.access-key", "access-key")
            .overrideConfigKey("quarkus.minio.secret-key", "secret-key")
            .overrideConfigKey("quarkus.minio.region", "us-east-1")
            .overrideConfigKey("quarkus.minio.telemetry.enabled", "true")
            .overrideConfigKey("quarkus.otel.traces.exporter", "none");

    @ConfigProperty(name = "quarkus.minio.port")
    int minioPort;

    @Inject
    MinioClient minioClient;

    @Inject
    MinioAsyncClient minioAsyncClient;

    @Inject
    InMemorySpanExporter exporter;

    @Inject
    OpenTelemetry openTelemetry;

    @Inject
    MiniosBuildTimeConfiguration miniosBuildTimeConfiguration;

    @BeforeEach
    void reset() {
        exporter.reset();
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop(0);
    }

    @Test
    void shouldNotCreateTelemetryNamedClient() {
        assertThat(miniosBuildTimeConfiguration.getMinioClients())
                .containsOnlyKeys(MiniosBuildTimeConfiguration.DEFAULT_MINIOCLIENT_NAME);
    }

    @Test
    void shouldProduceClientSpan() throws Exception {
        assertThat(minioClient.bucketExists(BucketExistsArgs.builder().bucket("test").build())).isTrue();

        SpanData span = awaitSingleSpan();
        assertThat(span.getKind()).isEqualTo(SpanKind.CLIENT);
        assertThat(span.getName()).isEqualTo("HEAD");
        assertThat(span.getInstrumentationScopeInfo().getName()).isEqualTo(TelemetryInterceptor.INSTRUMENTATION_NAME);
        assertThat(span.getAttributes().get(AttributeKey.stringKey("http.request.method"))).isEqualTo("HEAD");
        assertThat(span.getAttributes().get(AttributeKey.longKey("http.response.status_code"))).isEqualTo(200L);
        assertThat(span.getAttributes().get(AttributeKey.stringKey("server.address"))).isEqualTo("localhost");
        assertThat(span.getAttributes().get(AttributeKey.longKey("server.port")))
                .isEqualTo((long) minioPort);
        assertThat(span.getAttributes().get(AttributeKey.stringKey("url.full"))).endsWith("/test");
        assertThat(span.getAttributes().get(TelemetryInterceptor.MINIO_CLIENT_NAME))
                .isEqualTo(MiniosBuildTimeConfiguration.DEFAULT_MINIOCLIENT_NAME);
    }

    @Test
    void shouldAttachAsyncClientSpanToCurrentTrace() throws Exception {
        Span parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
        try (Scope ignored = parent.makeCurrent()) {
            assertThat(minioAsyncClient.bucketExists(BucketExistsArgs.builder().bucket("test").build()).get()).isTrue();
        } finally {
            parent.end();
        }

        await().untilAsserted(() -> assertThat(exporter.getFinishedSpanItems()).hasSize(2));
        SpanData span = exporter.getFinishedSpanItems().stream()
                .filter(s -> s.getKind() == SpanKind.CLIENT)
                .findFirst().orElseThrow();
        assertThat(span.getTraceId()).isEqualTo(parent.getSpanContext().getTraceId());
        assertThat(span.getParentSpanId()).isEqualTo(parent.getSpanContext().getSpanId());
    }

    private SpanData awaitSingleSpan() {
        await().untilAsserted(() -> assertThat(exporter.getFinishedSpanItems()).hasSize(1));
        List<SpanData> spans = exporter.getFinishedSpanItems();
        return spans.get(0);
    }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @ApplicationScoped
    static class InMemorySpanExporterProducer {
        @Produces
        @Singleton
        InMemorySpanExporter inMemorySpanExporter() {
            return InMemorySpanExporter.create();
        }

        @Produces
        @Singleton
        SpanProcessor spanProcessor(InMemorySpanExporter exporter) {
            return SimpleSpanProcessor.create(exporter);
        }
    }
}
