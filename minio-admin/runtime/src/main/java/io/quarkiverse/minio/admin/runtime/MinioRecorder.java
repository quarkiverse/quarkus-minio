package io.quarkiverse.minio.admin.runtime;

import java.util.function.Supplier;

import io.minio.admin.MinioAdminClient;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MinioRecorder {

    public Supplier<MinioAdminClient> minioAdminClientSupplier(String minioAdminClientName) {
        return () -> MinioAdminClients.fromName(minioAdminClientName);
    }

}
