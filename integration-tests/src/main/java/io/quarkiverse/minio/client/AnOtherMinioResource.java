package io.quarkiverse.minio.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

@Path("/another-minio")
public class AnOtherMinioResource {

    // Over sizing chunks
    private static final long PART_SIZE = 50 * 1024 * 1024;
    public static final String BUCKET_NAME = "another-test";

    @Inject
    @MinioQualifier("another")
    MinioClient minioClient;

    @ConfigProperty(name = "quarkus.minio.host")
    String minioHost;

    @POST
    public String addObject(@QueryParam("name") String fileName) throws IOException, MinioException, GeneralSecurityException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }
        try (InputStream is = new ByteArrayInputStream((minioHost.getBytes()))) {
            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(fileName)
                            .contentType("text/xml") // TODO : Parametrize
                            .stream(is, -1, PART_SIZE)
                            .build());
            return response.bucket() + "/" + response.object();
        } catch (MinioException | GeneralSecurityException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
