package org.webpieces.googlecloud.storage.api;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.inject.ImplementedBy;
import org.webpieces.googlecloud.storage.impl.raw.GCPRawStorageImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

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
@ImplementedBy(GCPRawStorageImpl.class)
public interface GCPRawStorage {

    Bucket get(String bucket, Storage.BucketGetOption... options);

    GCPBlob get(String bucket, String blob, Storage.BlobGetOption... options);

    Page<GCPBlob> list(String bucket, Storage.BlobListOption... options);

    boolean delete(String bucket, String blob, Storage.BlobSourceOption... options);

    byte[] readAllBytes(String bucket, String blob, Storage.BlobSourceOption... options);

    ReadableByteChannel reader(String bucket, String blob, Storage.BlobSourceOption... options);

    WritableByteChannel writer(BlobInfo blobInfo, Storage.BlobWriteOption... options);

    CopyWriter copy(Storage.CopyRequest copyRequest) throws IOException;
}
