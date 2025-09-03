package io.quarkiverse.minio.client;

import java.util.function.Supplier;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MinioRecorder {

    public Supplier<MinioClient> minioClientSupplier(String minioClientName) {
        return () -> MinioClients.fromName(minioClientName);
    }

    public Supplier<MinioAsyncClient> minioAsyncClientSupplier(String minioClientName) {
        return () -> MinioClients.fromNameAsync(minioClientName);
    }
}
