package io.quarkiverse.minio.admin.runtime;

import java.util.function.Supplier;

import io.minio.admin.MinioAdminClient;
import io.quarkiverse.minio.client.MiniosRuntimeConfiguration;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MinioRecorder {

    public Supplier<MinioAdminClient> minioAdminClientSupplier(String minioAdminClientName,
            @SuppressWarnings("unused") MiniosRuntimeConfiguration minioRuntimeConfig) {
        return () -> MinioAdminClients.fromName(minioAdminClientName);
    }

}
