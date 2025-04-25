package io.quarkiverse.minio.client;

import java.io.IOException;
import java.security.GeneralSecurityException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.micrometer.core.annotation.Timed;
import io.minio.admin.MinioAdminClient;

@Path("/adminClient")
public class AdminClientController {

    @Inject
    MinioAdminClient minioAdminClient;

    @GET
    @Timed(histogram = true)
    public String getServerInfo() throws IOException, GeneralSecurityException {

        return minioAdminClient.getServerInfo().toString();

    }

}
