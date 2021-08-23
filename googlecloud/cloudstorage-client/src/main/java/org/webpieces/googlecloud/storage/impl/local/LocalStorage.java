package org.webpieces.googlecloud.storage.impl.local;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.webpieces.googlecloud.storage.api.GCPBlob;
import org.webpieces.googlecloud.storage.api.GCPRawStorage;
import org.webpieces.googlecloud.storage.impl.ChannelWrapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class LocalStorage implements GCPRawStorage {
    public static final String LOCAL_BUILD_DIR = "build/local-cloudstorage/";
    private ChannelWrapper wrapper;

    @Inject
    public LocalStorage(ChannelWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public Bucket get(String bucket, Storage.BucketGetOption... options) {
        throw new UnsupportedOperationException("Need to implement this still");
    }

    @Override
    public GCPBlob get(String bucket, String blob, Storage.BlobGetOption... options) {
        // If we find bucket and blob, we will return the value. Otherwise, we will return null.
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(bucket + "/" + blob);
        if(in != null) {
            return new LocalGCPBlobImpl(bucket, blob);
        }

        File file = new File(LOCAL_BUILD_DIR + bucket + "/" + blob);
        if(file.exists()) {
            return new LocalGCPBlobImpl(bucket, blob);
        }

        return null;
    }

    @Override
    public Page<GCPBlob> list(String bucket, Storage.BlobListOption... options) {
        File file = new File(LOCAL_BUILD_DIR+bucket);
        if(!file.exists())
            file.mkdirs();
        return new LocalPage(bucket, file);
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
    public ReadableByteChannel reader(String bucket, String blob, Storage.BlobSourceOption... options) {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(bucket + "/" + blob);
        if(in != null) {
            ReadableByteChannel channel = Channels.newChannel(in);
            return channel;
        }

        //read from build directory
        File file = new File(LOCAL_BUILD_DIR + bucket + "/" + blob);
        try {
            InputStream i = new FileInputStream(file);
            ReadableByteChannel channel = Channels.newChannel(i);
            return channel;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WritableByteChannel writer(BlobInfo blobInfo, Storage.BlobWriteOption... options) {
        String bucket = blobInfo.getBlobId().getBucket();
        String name = blobInfo.getBlobId().getName();
        File file = new File(LOCAL_BUILD_DIR + bucket + "/" + name);
        try {
            File dir = file.getParentFile();
            if(!dir.exists())
                dir.mkdirs();
//            if(!file.exists())
//                file.createNewFile();
            OutputStream o = new FileOutputStream(file);
            WritableByteChannel writableByteChannel = Channels.newChannel(o);
            return writableByteChannel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CopyWriter copy(Storage.CopyRequest copyRequest) {
        throw new UnsupportedOperationException("Need to implement this still");
    }
}
