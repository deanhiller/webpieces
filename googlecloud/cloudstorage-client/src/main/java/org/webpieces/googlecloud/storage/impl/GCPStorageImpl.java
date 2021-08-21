package org.webpieces.googlecloud.storage.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.webpieces.googlecloud.storage.api.GCPRawStorage;
import org.webpieces.googlecloud.storage.api.GCPStorage;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Since tests mock rawStorage, changes to this class get included in testing.
 * THIS IS A GOOD THING ^^^^.   Do not break people
 */
@Singleton
public class GCPStorageImpl implements GCPStorage {

    private GCPRawStorage rawStorage;
    private ClientAssertions clientAssertions;
    private ChannelWrapper channelWrapper;

    @Inject
    public GCPStorageImpl(GCPRawStorage rawStorage, ClientAssertions clientAssertions, ChannelWrapper channelWrapper) {
        this.rawStorage = rawStorage;
        this.clientAssertions = clientAssertions;
        this.channelWrapper = channelWrapper;
    }


    @Override
    public Bucket get(String bucket, Storage.BucketGetOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.get(bucket, options);
    }

    @Override
    public Blob get(String bucket, String blob, Storage.BlobGetOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.get(bucket, blob, options);
    }

    @Override
    public Page<Blob> list(String bucket, Storage.BlobListOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.list(bucket, options);
    }

    @Override
    public boolean delete(String bucket, String blob, Storage.BlobSourceOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.delete(bucket, blob, options);
    }

    @Override
    public byte[] readAllBytes(String bucket, String blob, Storage.BlobSourceOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.readAllBytes(bucket, blob, options);
    }

    @Override
    public ReadChannel reader(String bucket, String blob, Storage.BlobSourceOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return channelWrapper.newChannelProxy(ReadChannel.class, rawStorage.reader(bucket, blob, options));
    }

    @Override
    public WriteChannel writer(BlobInfo blobInfo, Storage.BlobWriteOption... options) {
        clientAssertions.throwIfCannotGoRemote();
        return channelWrapper.newChannelProxy(WriteChannel.class, rawStorage.writer(blobInfo, options));
    }

    @Override
    public CopyWriter copy(Storage.CopyRequest copyRequest) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.copy(copyRequest);
    }
}
