package io.quarkiverse.minio.client;

import java.util.Optional;

import javax.inject.Singleton;

import okhttp3.OkHttpClient;

@Singleton
public class EmptyHttpClientProducer implements OptionalHttpClientProducer {

    @Override
    public Optional<OkHttpClient> apply(String minioClientName) {
        return Optional.empty();
    }
}
