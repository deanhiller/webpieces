package org.webpieces.googlecloud.storage.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.webpieces.googlecloud.storage.api.GCPRawStorage;

public class LocalStorage implements GCPRawStorage {

    @Override
    public Bucket get(String bucket, Storage.BucketGetOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public Blob get(String bucket, String blob, Storage.BlobGetOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public Page<Blob> list(String bucket, Storage.BlobListOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public boolean delete(String bucket, String blob, Storage.BlobSourceOption... options)
    {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public byte[] readAllBytes(String bucket, String blob, Storage.BlobSourceOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public ReadChannel reader(String bucket, String blob, Storage.BlobSourceOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public WriteChannel writer(BlobInfo blobInfo, Storage.BlobWriteOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public CopyWriter copy(Storage.CopyRequest copyRequest) {
        throw new UnsupportedOperationException("Need to implement this still");
    }
}
