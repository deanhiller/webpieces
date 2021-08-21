package org.webpieces.googlecloud.storage.impl2;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.webpieces.googlecloud.storage.api2.GCPRawStorage;

public class LocalStorage implements GCPRawStorage {
    @Override
    public Blob create(BlobInfo blobInfo, Storage.BlobTargetOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public Blob create(BlobInfo blobInfo, byte[] content, Storage.BlobTargetOption... options) {
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
    public boolean delete(String bucket, String blob, Storage.BlobSourceOption... options) {
        return false;
    }

    @Override
    public byte[] readAllBytes(String bucket, String blob, Storage.BlobSourceOption... options) {
        return new byte[0];
    }

    @Override
    public ReadChannel reader(String bucket, String blob, Storage.BlobSourceOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public WriteChannel writer(BlobInfo blobInfo, Storage.BlobWriteOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }
}
