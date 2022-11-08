package io.quarkiverse.minio.client;

import java.util.Optional;
import java.util.function.Function;

import okhttp3.OkHttpClient;

public interface OptionalHttpClientProducer extends Function<String, Optional<OkHttpClient>> {
}
