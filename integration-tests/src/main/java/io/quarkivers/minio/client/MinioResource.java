package io.quarkivers.minio.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

@Path("/minio")
public class MinioResource {

    // Over sizing chunks
    private static final long PART_SIZE = 50 * 1024 * 1024;
    public static final String BUCKET_NAME = "test";

    @Inject
    MinioClient minioClient;

    @POST
    public String addObject(@QueryParam("name") String fileName) throws IOException, MinioException, GeneralSecurityException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }
        String dummyFile = "Dummy content";
        try (InputStream is = new ByteArrayInputStream((dummyFile.getBytes()))) {
            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("test")
                            .object(fileName)
                            .contentType("text/xml") // TODO : Parametrize
                            .stream(is, -1, PART_SIZE)
                            .build());
            GetObjectResponse test = minioClient.getObject(GetObjectArgs.builder().bucket("test").object(fileName).build());
            return test.bucket() + "/" + test.object();
        } catch (MinioException | GeneralSecurityException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
