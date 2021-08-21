package org.webpieces.googlecloud.storage.api2;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

/**
 * With the switch to the underlying google.Storage interface that is mockable, the implementation
 * of GCPStorage here is ONE TO ONE since we cannot test ANY code behind this interface as we
 * swap it out with a mock object to test our systems.
 *
 * THIS IS WHAT YOU SHOULD MOCK!!!  It is the lowest level
 */
public interface GCPRawStorage {

    Blob create(BlobInfo blobInfo, Storage.BlobTargetOption... options);

    Blob create(BlobInfo blobInfo, byte[] content, Storage.BlobTargetOption... options);

    Blob get(String bucket, String blob, Storage.BlobGetOption... options);

    Page<Blob> list(String bucket, Storage.BlobListOption... options);

    boolean delete(String bucket, String blob, Storage.BlobSourceOption... options);

    byte[] readAllBytes(String bucket, String blob, Storage.BlobSourceOption... options);

    ReadChannel reader(String bucket, String blob, Storage.BlobSourceOption... options);

    WriteChannel writer(BlobInfo blobInfo, Storage.BlobWriteOption... options);
}
