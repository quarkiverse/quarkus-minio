package io.quarkiverse.minio.client;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.bouncycastle.crypto.InvalidCipherTextException;

import io.micrometer.core.annotation.Timed;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.UserInfo;

@Path("/adminClient")
public class AdminClientController {

    @Inject
    MinioAdminClient minioAdminClient;

    @GET
    @Timed(histogram = true)
    public String getServerInfo() throws IOException, GeneralSecurityException {

        return minioAdminClient.getServerInfo().toString();

    }

    @GET
    @Path("/user")
    @Timed(histogram = true)
    public String createAndGetUser() throws IOException, GeneralSecurityException, InvalidCipherTextException {
        minioAdminClient.addUser("testuser", UserInfo.Status.ENABLED, "secretKey", null, List.of());
        minioAdminClient.getUserInfo("testuser");
        minioAdminClient.deleteUser("testuser");
        return "added and deleted user";
    }

}
