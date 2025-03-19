package io.quarkiverse.minio.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.micrometer.core.annotation.Timed;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.ResponseDate;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import io.smallrye.mutiny.Uni;

@Path("/minio")
public class MinioController {

    // Over sizing chunks
    private static final long PART_SIZE = 50 * 1024 * 1024;
    public static final String BUCKET_NAME = "test";

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "quarkus.minio.url")
    String minioUrl;

    @POST
    @Timed(histogram = true)
    public String addObject(@QueryParam("name") String fileName) throws IOException, MinioException, GeneralSecurityException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }
        try (InputStream is = new ByteArrayInputStream((minioUrl.getBytes()))) {
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

    @GET
    public String getObject(@QueryParam("name") String fileName) {
        try (InputStream is = new ByteArrayInputStream((minioUrl.getBytes()))) {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(fileName)
                            .build());
            return response.bucket() + "/" + response.object();
        } catch (MinioException | GeneralSecurityException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @GET
    @Path("/{prefix}")
    public Uni<Void> setBucketLifecycle(String prefix) {
        final Expiration expiration = new Expiration((ResponseDate) null, 8, null);
        final LifecycleRule rule = new LifecycleRule(Status.ENABLED, null, expiration,
                new RuleFilter(prefix),
                "ExpirationObjectsRule", null, null, null);
        final LifecycleConfiguration lifecycleConfiguration = new LifecycleConfiguration(List.of(rule));
        final SetBucketLifecycleArgs setBucketLifecycleArgs = SetBucketLifecycleArgs.builder()
                .bucket(BUCKET_NAME)
                .config(lifecycleConfiguration)
                .build();
        try {
            this.minioClient.setBucketLifecycle(setBucketLifecycleArgs); // $$$$$$$ ERROR HERE ON NATIVE MODE $$$$$$$
            return Uni.createFrom().voidItem();
        } catch (final InsufficientDataException | InternalException | InvalidKeyException | IOException
                | NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InvalidResponseException
                | ServerException e) {
            e.printStackTrace();
            return Uni.createFrom().voidItem();
        }
    }

}
