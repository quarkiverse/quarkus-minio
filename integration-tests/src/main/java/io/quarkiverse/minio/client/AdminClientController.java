package io.quarkiverse.minio.client;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.micrometer.core.annotation.Timed;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.Status;
import io.minio.admin.UserInfo;
import io.minio.errors.MinioException;

@Path("/adminClient")
public class AdminClientController {

    private static final String TEST_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Sid": "ReadOnlyAccess",
                  "Effect": "Allow",
                  "Principal": "*",
                  "Action": "s3:GetObject",
                  "Resource": "arn:aws:s3:::bucket/*"
                }
              ]
            }
            """;

    @Inject
    MinioAdminClient minioAdminClient;

    @GET
    @Timed(histogram = true)
    public String getServerInfo() throws MinioException {

        return minioAdminClient.getServerInfo().toString();

    }

    @GET
    @Path("/user")
    @Timed(histogram = true)
    public String createAndGetUser() throws MinioException {
        minioAdminClient.addUser("testuser", Status.ENABLED, "secretKey", null, List.of());
        UserInfo userInfo = minioAdminClient.getUserInfo("testuser");
        if (userInfo == null) {
            throw new IllegalStateException("User should not be null");
        }
        if (!userInfo.status().equals(Status.ENABLED)) {
            throw new IllegalStateException("User should be enabled");
        }
        minioAdminClient.deleteUser("testuser");
        return "added and deleted user";
    }

    @GET
    @Path("/user-policy")
    @Timed(histogram = true)
    public String createAndAssignPolicyToUser() throws MinioException {
        minioAdminClient.addUser("test-user-policy", Status.ENABLED, "secretKey", null, List.of());
        minioAdminClient.addCannedPolicy("test-policy-user", TEST_POLICY);
        minioAdminClient.setPolicy("test-user-policy", false, "test-policy-user");
        return "added assigned policy to user";
    }

    @GET
    @Path("/group-policy")
    @Timed(histogram = true)
    public String createAndAssignPolicyToGroup() throws MinioException {
        minioAdminClient.addUpdateGroup("test-group-policy", Status.ENABLED, List.of());
        minioAdminClient.addCannedPolicy("test-policy-group", TEST_POLICY);
        minioAdminClient.setPolicy("test-group-policy", true, "test-policy-group");
        return "added assigned policy to group";
    }

}
