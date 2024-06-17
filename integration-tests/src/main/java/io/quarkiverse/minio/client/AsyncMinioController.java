package io.quarkiverse.minio.client;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.minio.BucketExistsArgs;
import io.minio.MinioAsyncClient;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.smallrye.mutiny.Uni;

@Path("/async-minio")
public class AsyncMinioController {
    @Inject
    MinioAsyncClient minioAsyncClient;

    @GET
    public Uni<String> getBucketExists(@QueryParam("name") String bucketName) throws InsufficientDataException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException {
        return Uni
                .createFrom()
                .completionStage(
                        minioAsyncClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
                .map(result -> result ? "Bucket exists" : "Bucket doesn't exist");
    }
}
