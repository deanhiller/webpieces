package org.webpieces.aws.storage.impl.raw;

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import org.webpieces.aws.storage.api.AWSBlob;
import org.webpieces.aws.storage.api.AWSRawStorage;
import org.webpieces.aws.storage.impl.S3WritableByteChannel;
import org.webpieces.aws.storage.impl.StorageSupplier;
import org.webpieces.util.SingletonSupplier;

/**
 * ADD NO CODE to this class as it is not tested until integration time.  If it is 1 to 1,
 * there is no testing to do and we rely on google's testing of Storage.java they have
 */
@Singleton
public class AWSRawStorageImpl implements AWSRawStorage { //implements Storage {

    private SingletonSupplier<S3Client> storage;

    @Inject
    public AWSRawStorageImpl(StorageSupplier storage) {
        this.storage = new SingletonSupplier<>(storage);
    }

    @Override
    public AWSBlob get(String bucket, String key) {

        try {

            HeadObjectResponse response = storage.get().headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            String contentType = response.contentType();
            long size = response.contentLength();

            return new AWSBlobImpl(bucket, key, contentType, size);
        } catch(NoSuchKeyException ex) {
            return null;
        }

    }

    @Override
    public Stream<AWSBlob> list(String bucket) {
        return storage.get().listObjectsV2Paginator(ListObjectsV2Request.builder().bucket(bucket).build()).contents().stream().map(obj -> new AWSBlobImpl(bucket, obj.key(), null, obj.size()));
    }

    @Override
    public boolean delete(String bucket, String key) {
        try {
            storage.get().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch(AwsServiceException ex) {
            return false;
        }
    }

    @Override
    public byte[] readAllBytes(String bucket, String key) {
        return storage.get().getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build()).asByteArray();
    }

    @Override
    public ReadableByteChannel reader(String bucket, String key) {
        return Channels.newChannel(storage.get().getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()));
    }

    @Override
    public WritableByteChannel writer(String bucket, String key) {
        return new S3WritableByteChannel(storage.get(), bucket, key);
    }

    @Override
    public boolean copy(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        try {
            storage.get().copyObject(CopyObjectRequest.builder().sourceBucket(sourceBucket).sourceKey(sourceKey).destinationBucket(destBucket).destinationKey(destKey).build());
            return true;
        } catch(AwsServiceException ex) {
            return false;
        }
    }

}
