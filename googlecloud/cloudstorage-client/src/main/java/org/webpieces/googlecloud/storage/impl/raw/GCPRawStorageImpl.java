package org.webpieces.googlecloud.storage.impl.raw;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Storage.BlobGetOption;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.BlobSourceOption;
import com.google.cloud.storage.Storage.BlobWriteOption;
import org.webpieces.googlecloud.storage.api.CopyInterface;
import org.webpieces.googlecloud.storage.api.GCPBlob;
import org.webpieces.googlecloud.storage.api.GCPRawStorage;
import org.webpieces.googlecloud.storage.impl.StorageSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * ADD NO CODE to this class as it is not tested until integration time.  If it is 1 to 1,
 * there is no testing to do and we rely on google's testing of Storage.java they have
 */
@Singleton
public class GCPRawStorageImpl implements GCPRawStorage { //implements Storage {

    private Supplier<Storage> storage;

    @Inject
    public GCPRawStorageImpl(StorageSupplier storage) {
        this.storage = storage;
    }

    @Override
    public Bucket get(String bucket, Storage.BucketGetOption... options) {
        return storage.get().get(bucket, options);
    }

    @Override
    public GCPBlob get(String bucket, String blob, BlobGetOption... options) {
        Blob blob1 = storage.get().get(bucket, blob, options);
        return new GCPBlobImpl(blob1);
    }

    @Override
    public Page<GCPBlob> list(String bucket, BlobListOption... options) {

        Page<Blob> list = storage.get().list(bucket, options);
        return new GCPPageImpl(list);
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

    @Override
    public CopyInterface copy(Storage.CopyRequest copyRequest) {
        CopyWriter copyWriter = storage.get().copy(copyRequest);
        return new CopyWriterImpl(copyWriter);
    }

}
