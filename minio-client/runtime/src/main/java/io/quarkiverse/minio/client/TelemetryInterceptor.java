package io.quarkiverse.minio.client;

import java.io.IOException;
import java.util.List;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpClientAttributesExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpClientAttributesGetter;
import io.opentelemetry.instrumentation.api.semconv.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpSpanStatusExtractor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp interceptor creating an OpenTelemetry {@code CLIENT} span, following the HTTP client semantic conventions,
 * for every request sent to the minio server.
 * <p>
 * The trace context is intentionally not injected in the request headers, as requests are already signed.
 */
public class TelemetryInterceptor implements Interceptor {

    public static final String INSTRUMENTATION_NAME = "io.quarkiverse.minio";

    public static final AttributeKey<String> MINIO_CLIENT_NAME = AttributeKey.stringKey("minio.client.name");

    private final Instrumenter<Request, Response> instrumenter;

    public TelemetryInterceptor(OpenTelemetry openTelemetry, String minioClientName) {
        this.instrumenter = Instrumenter.<Request, Response> builder(openTelemetry, INSTRUMENTATION_NAME,
                HttpSpanNameExtractor.create(MinioHttpAttributesGetter.INSTANCE))
                .setSpanStatusExtractor(HttpSpanStatusExtractor.create(MinioHttpAttributesGetter.INSTANCE))
                .addAttributesExtractor(HttpClientAttributesExtractor.create(MinioHttpAttributesGetter.INSTANCE))
                .addAttributesExtractor(AttributesExtractor.constant(MINIO_CLIENT_NAME, minioClientName))
                .buildInstrumenter(SpanKindExtractor.alwaysClient());
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Context parentContext = Context.current();
        if (!instrumenter.shouldStart(parentContext, request)) {
            return chain.proceed(request);
        }
        Context context = instrumenter.start(parentContext, request);
        Response response = null;
        Throwable error = null;
        try (Scope ignored = context.makeCurrent()) {
            response = chain.proceed(request);
            return response;
        } catch (IOException | RuntimeException e) {
            error = e;
            throw e;
        } finally {
            instrumenter.end(context, request, response, error);
        }
    }

    enum MinioHttpAttributesGetter implements HttpClientAttributesGetter<Request, Response> {
        INSTANCE;

        @Override
        public String getUrlFull(Request request) {
            return request.url().toString();
        }

        @Override
        public String getServerAddress(Request request) {
            return request.url().host();
        }

        @Override
        public Integer getServerPort(Request request) {
            return request.url().port();
        }

        @Override
        public String getHttpRequestMethod(Request request) {
            return request.method();
        }

        @Override
        public List<String> getHttpRequestHeader(Request request, String name) {
            return request.headers(name);
        }

        @Override
        public Integer getHttpResponseStatusCode(Request request, Response response, Throwable error) {
            return response == null ? null : response.code();
        }

        @Override
        public List<String> getHttpResponseHeader(Request request, Response response, String name) {
            return response == null ? List.of() : response.headers(name);
        }

        @Override
        public String getNetworkProtocolName(Request request, Response response) {
            return "http";
        }

        @Override
        public String getNetworkProtocolVersion(Request request, Response response) {
            if (response == null) {
                return null;
            }
            return switch (response.protocol()) {
                case HTTP_1_0 -> "1.0";
                case HTTP_1_1 -> "1.1";
                case HTTP_2, H2_PRIOR_KNOWLEDGE -> "2";
                default -> null;
            };
        }
    }
}
