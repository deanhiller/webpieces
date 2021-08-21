package org.webpieces.googlecloud.storage.impl2;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.Storage.BlobGetOption;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.BlobSourceOption;
import com.google.cloud.storage.Storage.BlobWriteOption;
import org.webpieces.googlecloud.storage.api2.GCPRawStorage;

import java.util.function.Supplier;

/**
 * ADD NO CODE to this class as it is not tested until integration time.  If it is 1 to 1,
 * there is no testing to do and we rely on google's testing of Storage.java they have
 */
public class GCPRawStorageImpl implements GCPRawStorage { //implements Storage {

    private Supplier<Storage> storage;

    @Override
    public Blob create(BlobInfo blobInfo, BlobTargetOption... options) {
        return storage.get().create(blobInfo, options);
    }

    @Override
    public Blob create(BlobInfo blobInfo, byte[] content, BlobTargetOption... options) {
        return storage.get().create(blobInfo, content, options);
    }

    @Override
    public Blob get(String bucket, String blob, BlobGetOption... options) {
        return storage.get().get(bucket, blob, options);
    }

    @Override
    public Page<Blob> list(String bucket, BlobListOption... options) {
        return storage.get().list(bucket, options);
    }

    @Override
    public boolean delete(String bucket, String blob, BlobSourceOption... options) {
        return storage.get().delete(bucket, blob, options);
    }

    @Override
    public byte[] readAllBytes(String bucket, String blob, BlobSourceOption... options) {
        return storage.get().readAllBytes(bucket, blob, options);
    }

    @Override
    public ReadChannel reader(String bucket, String blob, BlobSourceOption... options) {
        return storage.get().reader(bucket, blob, options);
    }

    @Override
    public WriteChannel writer(BlobInfo blobInfo, BlobWriteOption... options) {
        return storage.get().writer(blobInfo, options);
    }
}
