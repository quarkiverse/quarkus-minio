package io.quarkiverse.minio.client;

import java.util.function.Supplier;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MinioRecorder {

    public Supplier<MinioClient> minioClientSupplier(String minioClientName,
                                                     @SuppressWarnings("unused") MiniosRuntimeConfiguration minioRuntimeConfig) {
        return () -> MinioClients.fromName(minioClientName);
    }

    public Supplier<MinioAsyncClient> minioAsyncClientSupplier(String minioClientName,
                                                               @SuppressWarnings("unused") MiniosRuntimeConfiguration minioRuntimeConfig) {
        return () -> MinioClients.fromNameAsync(minioClientName);
    }

    public Supplier<MutinyMinioClient> mutinyMinioClientSupplier(
            String minioClientName,
            @SuppressWarnings("unused") MiniosRuntimeConfiguration minioRuntimeConfig
    ) {
        return () -> MinioClients.fromNameMutiny(minioClientName);
    }
}
