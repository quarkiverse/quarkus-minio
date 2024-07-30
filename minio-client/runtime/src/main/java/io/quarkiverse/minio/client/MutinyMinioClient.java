package io.quarkiverse.minio.client;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static io.smallrye.mutiny.Uni.createFrom;

public class MutinyMinioClient {
    private final MinioAsyncClient minioAsyncClient;

    public MutinyMinioClient(MinioAsyncClient minioAsyncClient) {
        this.minioAsyncClient = minioAsyncClient;
    }

    public Uni<GetObjectResponse> getObject(GetObjectArgs args) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException {
        return createFrom().completionStage(minioAsyncClient.getObject(args));
    }

    public Uni<StatObjectResponse> statObject(StatObjectArgs statObjectArgs) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException {
        return createFrom().completionStage(minioAsyncClient.statObject(statObjectArgs));
    }

    public Uni<Void> downloadObject(DownloadObjectArgs downloadObjectArgs) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException {
        return createFrom().completionStage(minioAsyncClient.downloadObject(downloadObjectArgs));
    }

    public Uni<ObjectWriteResponse> copyObject(CopyObjectArgs copyObjectArgs) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException {
        return createFrom().completionStage(minioAsyncClient.copyObject(copyObjectArgs));
    }

    public Uni<ObjectWriteResponse> composeObject(ComposeObjectArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.composeObject(args));
    }

    public Uni<String> getPresignedObjectUrl(GetPresignedObjectUrlArgs args) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, XmlParserException, ServerException {
        return createFrom().item(minioAsyncClient.getPresignedObjectUrl(args));
    }

    public Uni<Map<String, String>> getPresignedPostFormData(PostPolicy policy) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return createFrom().item(minioAsyncClient.getPresignedPostFormData(policy));
    }

    public Uni<Void> removeObject(RemoveObjectArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.removeObject(args));
    }

    public Multi<Result<DeleteError>> removeObjects(final RemoveObjectsArgs args) {
        return Multi.createFrom().iterable(minioAsyncClient.removeObjects(args));
    }

    public Uni<Void> restoreObject(RestoreObjectArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.restoreObject(args));
    }

    public Uni<List<Bucket>> listBuckets(ListBucketsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.listBuckets(args));
    }

    public Uni<List<Bucket>> listBuckets() throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return listBuckets(ListBucketsArgs.builder().build());
    }

    public Uni<Boolean> bucketExists(BucketExistsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.bucketExists(args));
    }

    public Uni<Void> makeBucket(MakeBucketArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.makeBucket(args));
    }

    public Uni<Void> setBucketVersioning(SetBucketVersioningArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketVersioning(args));
    }

    public Uni<VersioningConfiguration> getBucketVersioning(GetBucketVersioningArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketVersioning(args));
    }

    public Uni<Void> setObjectLockConfiguration(SetObjectLockConfigurationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setObjectLockConfiguration(args));
    }

    public Uni<Void> deleteObjectLockConfiguration(DeleteObjectLockConfigurationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteObjectLockConfiguration(args));
    }

    public Uni<ObjectLockConfiguration> getObjectLockConfiguration(GetObjectLockConfigurationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getObjectLockConfiguration(args));
    }

    public Uni<Void> setObjectRetention(SetObjectRetentionArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setObjectRetention(args));
    }

    public Uni<Retention> getObjectRetention(GetObjectRetentionArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getObjectRetention(args));
    }

    public Uni<Void> enableObjectLegalHold(EnableObjectLegalHoldArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.enableObjectLegalHold(args));
    }

    public Uni<Void> disableObjectLegalHold(DisableObjectLegalHoldArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.disableObjectLegalHold(args));
    }

    public Uni<Boolean> isObjectLegalHoldEnabled(IsObjectLegalHoldEnabledArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.isObjectLegalHoldEnabled(args));
    }

    public Uni<Void> removeBucket(RemoveBucketArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.removeBucket(args));
    }

    public Uni<ObjectWriteResponse> putObject(PutObjectArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.putObject(args));
    }

    public Uni<ObjectWriteResponse> uploadObject(UploadObjectArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.uploadObject(args));
    }

    public Uni<String> getBucketPolicy(GetBucketPolicyArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketPolicy(args));
    }

    public Uni<Void> setBucketPolicy(SetBucketPolicyArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketPolicy(args));
    }

    public Uni<Void> deleteBucketPolicy(DeleteBucketPolicyArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteBucketPolicy(args));
    }

    public Uni<Void> setBucketLifecycle(SetBucketLifecycleArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketLifecycle(args));
    }

    public Uni<Void> deleteBucketLifecycle(DeleteBucketLifecycleArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteBucketLifecycle(args));
    }

    public Uni<LifecycleConfiguration> getBucketLifecycle(GetBucketLifecycleArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketLifecycle(args));
    }

    public Uni<NotificationConfiguration> getBucketNotification(GetBucketNotificationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketNotification(args));
    }

    public Uni<Void> setBucketNotification(SetBucketNotificationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketNotification(args));
    }

    public Uni<Void> deleteBucketNotification(DeleteBucketNotificationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteBucketNotification(args));
    }

    public Uni<ReplicationConfiguration> getBucketReplication(GetBucketReplicationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketReplication(args));
    }

    public Uni<Void> setBucketReplication(SetBucketReplicationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketReplication(args));
    }

    public Uni<Void> deleteBucketReplication(DeleteBucketReplicationArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteBucketReplication(args));
    }

    public Uni<SelectResponseStream> selectObjectContent(SelectObjectContentArgs args) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return createFrom().item(minioAsyncClient.selectObjectContent(args));
    }

    public Uni<Void> setBucketEncryption(SetBucketEncryptionArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketEncryption(args));
    }

    public Uni<SseConfiguration> getBucketEncryption(GetBucketEncryptionArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketEncryption(args));
    }

    public Uni<Void> deleteBucketEncryption(DeleteBucketEncryptionArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteBucketEncryption(args));
    }

    public Uni<Tags> getBucketTags(GetBucketTagsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getBucketTags(args));
    }

    public Uni<Void> setBucketTags(SetBucketTagsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setBucketTags(args));
    }

    public Uni<Void> deleteBucketTags(DeleteBucketTagsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteBucketTags(args));
    }

    public Uni<Tags> getObjectTags(GetObjectTagsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.getObjectTags(args));
    }

    public Uni<Void> setObjectTags(SetObjectTagsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.setObjectTags(args));
    }

    public Uni<Void> deleteObjectTags(DeleteObjectTagsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.deleteObjectTags(args));
    }

    public Uni<ObjectWriteResponse> uploadSnowballObjects(UploadSnowballObjectsArgs args) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return createFrom().completionStage(minioAsyncClient.uploadSnowballObjects(args));
    }

}
