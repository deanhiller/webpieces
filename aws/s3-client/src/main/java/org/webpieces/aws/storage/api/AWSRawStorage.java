package org.webpieces.aws.storage.api;

import com.google.inject.ImplementedBy;
import software.amazon.awssdk.services.s3.model.Bucket;

import org.webpieces.aws.storage.impl.raw.AWSRawStorageImpl;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.stream.Stream;

/**
 * Create as you need GCPRawStorage methods - create in
 * GCPRawStorage, GCPStorage, GCPRawStorageImpl, LocalStorage for each
 * method as we go.
 *
 * With the switch to the underlying google.Storage interface that is mockable, the implementation
 * of GCPStorage here is ONE TO ONE since we cannot test ANY code behind this interface as we
 * swap it out with a mock object to test our systems.
 *
 * THIS IS WHAT YOU SHOULD MOCK!!!  It is the lowest level AND 1 to 1 to Google Storage so
 * we do not have to test anything as Google will test it for us.
 */

//GCPRawStorageImpl for production, LocalStorage for local dev
@ImplementedBy(AWSRawStorageImpl.class)
public interface AWSRawStorage {

    AWSBlob get(String bucket, String blob);

    Stream<AWSBlob> list(String bucket);

    boolean delete(String bucket, String key);

    byte[] readAllBytes(String bucket, String key);

    ReadableByteChannel reader(String bucket, String key);

    WritableByteChannel writer(String bucket, String key);

    boolean copy(String sourceBucket, String sourceKey, String destBucket, String destKey);
}
