package org.webpieces.googlecloud.storage.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.webpieces.api.Environment;
import org.webpieces.googlecloud.storage.BlobFile;
import org.webpieces.googlecloud.storage.ChannelInvocationHandler;
import org.webpieces.googlecloud.storage.GCPStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.plugin.hibernate.Em;
import org.webpieces.util.SingletonSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Singleton
public class GCPStorageImpl implements GCPStorage, Serializable {

    private static final Logger log = LoggerFactory.getLogger(GCPStorageImpl.class);

    private final Environment environment;

    private Supplier<Storage> storage;

    @Inject
    public GCPStorageImpl(final Environment environment) {
        this.environment = environment;
        this.storage = new SingletonSupplier<>(new StorageSupplier());
    }

    @Override
    public BlobFile getBlobFile(String bucket, String filename) {

        assertNotInTransaction();

        String prefixedBucket = getPrefixedBucket(bucket);
        Blob blob = storage.get().get(prefixedBucket, filename);

        if (blob == null) {
            throw SneakyThrow.sneak(new FileNotFoundException("Blob not found! bucket=" + prefixedBucket + " filename=" + filename));
        }

        return new BlobFile(blob.getBucket(), blob.getName(), blob.getContentType(), blob.getSize(), blob::reader, blob::writer);

    }

    @Override
    public URI getFileInZip(String bucket, String filename) {
        return URI.create("jar:gs://" + bucket + "/" + filename);
    }

    @Override
    public WritableByteChannel create(String bucketName, String filename) {

        assertNotInTransaction();

        WriteChannel writer = storage.get().writer(BlobInfo.newBuilder(bucketName, filename).build(), Storage.BlobWriteOption.detectContentType());
        return ChannelInvocationHandler.newChannelProxy(WritableByteChannel.class, writer);
    }

    @Override
    public WritableByteChannel create(String bucketName, String filename, String mimeType) {

        assertNotInTransaction();

        WriteChannel writer = storage.get().writer(BlobInfo.newBuilder(bucketName, filename).setContentType(mimeType).build());
        return ChannelInvocationHandler.newChannelProxy(WritableByteChannel.class, writer);
    }

    @Override
    public List<String> list(String bucket) {

        assertNotInTransaction();

        Iterable<Blob> blobs = storage.get().list(bucket).iterateAll();
        List<String> blobNames = new ArrayList<>();
        for (Blob blob : blobs) {
            blobNames.add(blob.getName());
        }
        return blobNames;

    }

    @Override
    public void copy(BlobFile source, BlobFile destination) {

        assertNotInTransaction();

        storage.get().copy(new Storage.CopyRequest.Builder().
                setSource(BlobId.of(source.getBucket(), source.getName()))
                .setTarget(BlobId.of(destination.getBucket(), destination.getName()))
                .build()
        ).getResult();

    }

    @Override
    public void copy(BlobFile source, String destBucket, String destName) {

        assertNotInTransaction();

        String prefixedBucket = getPrefixedBucket(destBucket);
        storage.get().copy(new Storage.CopyRequest.Builder().
                setSource(BlobId.of(source.getBucket(), source.getName()))
                .setTarget(BlobId.of(prefixedBucket, destName))
                .build()
        ).getResult();

    }

    @Override
    public boolean delete(BlobFile blobFile) {

        assertNotInTransaction();

        return storage.get().delete(BlobId.of(blobFile.getBucket(), blobFile.getName()));

    }

    public List<String> listObjects(String bucket) {

        assertNotInTransaction();

        String prefixedBucket = getPrefixedBucket(bucket);
        Bucket gbucket = storage.get().get(prefixedBucket);
        Page<Blob> blobs = gbucket.list();

        List<String> blobList = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
            blobList.add(blob.getName());
        }
        return blobList;
    }


    /* HELPER METHODS */

    protected void assertNotInTransaction() {

        EntityManager em = Em.get();
        if (em != null) {
            throw new IllegalStateException("You should never make remote calls while in a transaction");
        }

    }

    private String getPrefixedBucket(String bucket) {

        //Note: I don't want to deal with this now. (4/23)
        if ("orderly-couchdrop".equals(bucket) && (environment == Environment.PRODUCTION)) {
            return bucket;
        }

        //Note: Remove this once the function is un-fucktioned (4/23)
        if ("orderly-standard-roster".equals(bucket) && (environment == Environment.PRODUCTION)) {
            return bucket;
        }

        return environment.getPrefix() + bucket;
    }

    private static class StorageSupplier implements Supplier<Storage>, Serializable {

        @Override
        public Storage get() {
            return StorageOptions.newBuilder().build().getService();
        }

    }

}
