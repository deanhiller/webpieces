package org.webpieces.googlecloud.storage.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.webpieces.googlecloud.storage.api.BlobFile;
import org.webpieces.googlecloud.storage.api.GCPStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.exceptions.SneakyThrow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Singleton
public class GCPStorageImpl implements GCPStorage, Serializable {

    private static final Logger log = LoggerFactory.getLogger(GCPStorageImpl.class);

    private Supplier<Storage> storage;
    private ClientAssertions clientAssertions;
    private ChannelWrapper channelWrapper;

    @Inject
    public GCPStorageImpl(final ClientAssertions clientAssertions, ChannelWrapper channelWrapper) {
        this.clientAssertions = clientAssertions;
        this.channelWrapper = channelWrapper;
        this.storage = new SingletonSupplier<>(new StorageSupplier());
    }

    @Override
    public BlobFile getBlobFile(String bucket, String filename) {

        clientAssertions.throwIfCannotGoRemote();

        Blob blob = storage.get().get(bucket, filename);

        if (blob == null) {
            throw SneakyThrow.sneak(new FileNotFoundException("Blob not found! bucket=" + bucket + " filename=" + filename));
        }

        //BIG NOTE: CANNOT call blob.writer here or it is invoked to capture it so must pass blob ref instead
        Supplier<WritableByteChannel> writableByteChannelSupplier = () -> channelWrapper.createWriter(blob);
        Supplier<ReadableByteChannel> readableByteChannelSupplier = () -> channelWrapper.createReader(blob);

        return new BlobFile(blob.getBucket(), blob.getName(), blob.getContentType(), blob.getSize(),
                readableByteChannelSupplier, writableByteChannelSupplier);

    }

//    @Override
//    public URI getFileInZip(String bucket, String filename) {
//        return URI.create("jar:gs://" + bucket + "/" + filename);
//    }

    @Override
    public WritableByteChannel create(String bucketName, String filename) {

        clientAssertions.throwIfCannotGoRemote();

        WriteChannel writer = storage.get().writer(BlobInfo.newBuilder(bucketName, filename).build(), Storage.BlobWriteOption.detectContentType());
        return channelWrapper.newChannelProxy(WritableByteChannel.class, writer);
    }

    @Override
    public WritableByteChannel create(String bucketName, String filename, String mimeType) {

        clientAssertions.throwIfCannotGoRemote();

        WriteChannel writer = storage.get().writer(BlobInfo.newBuilder(bucketName, filename).setContentType(mimeType).build());
        return channelWrapper.newChannelProxy(WritableByteChannel.class, writer);
    }

    @Override
    public List<String> list(String bucket) {

        clientAssertions.throwIfCannotGoRemote();

        Iterable<Blob> blobs = storage.get().list(bucket).iterateAll();
        List<String> blobNames = new ArrayList<>();
        for (Blob blob : blobs) {
            blobNames.add(blob.getName());
        }
        return blobNames;

    }

    @Override
    public void copy(BlobFile source, BlobFile destination) {

        clientAssertions.throwIfCannotGoRemote();

        storage.get().copy(new Storage.CopyRequest.Builder().
                setSource(BlobId.of(source.getBucket(), source.getName()))
                .setTarget(BlobId.of(destination.getBucket(), destination.getName()))
                .build()
        ).getResult();

    }

    @Override
    public void copy(BlobFile source, String destBucket, String destName) {

        clientAssertions.throwIfCannotGoRemote();

        storage.get().copy(new Storage.CopyRequest.Builder().
                setSource(BlobId.of(source.getBucket(), source.getName()))
                .setTarget(BlobId.of(destBucket, destName))
                .build()
        ).getResult();

    }

    @Override
    public boolean delete(BlobFile blobFile) {

        clientAssertions.throwIfCannotGoRemote();

        return storage.get().delete(BlobId.of(blobFile.getBucket(), blobFile.getName()));

    }

    public List<String> listObjects(String bucket) {

        clientAssertions.throwIfCannotGoRemote();

        Bucket gbucket = storage.get().get(bucket);
        Page<Blob> blobs = gbucket.list();

        List<String> blobList = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
            blobList.add(blob.getName());
        }
        return blobList;
    }

    private static class StorageSupplier implements Supplier<Storage>, Serializable {

        @Override
        public Storage get() {
            return StorageOptions.newBuilder().build().getService();
        }

    }

}
