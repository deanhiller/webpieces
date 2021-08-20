package org.webpieces.googlecloud.storage.impl;

import org.webpieces.api.Environment;
import org.webpieces.api.util.SneakyThrow;
import org.webpieces.googlecloud.storage.BlobFile;
import org.webpieces.googlecloud.storage.ChannelInvocationHandler;
import org.webpieces.googlecloud.storage.GCPStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Singleton
public class LocalStorage extends GCPStorageImpl implements GCPStorage, Serializable {

    private static final Logger log = LoggerFactory.getLogger(LocalStorage.class);

    public LocalStorage() {
        super(Environment.LOCAL);
    }

    @Override
    public BlobFile getBlobFile(String bucket, String filename) {

        assertNotInTransaction();

        if (filename == null) {
            throw new IllegalArgumentException("'filename' cannot be null!");
        }

        String contentType = "application/octet-stream";

        long size = 0;
        Path localFile = getLocalStoragePath(bucket, filename);
        if (Files.exists(localFile)) {
            try {
                size = Files.size(localFile);
            } catch (IOException ex) {
                throw SneakyThrow.sneak(ex);
            }
        }

        BlobFile blobFile = new BlobFile(bucket, filename, contentType, size,
                () -> getReadChannel(bucket, filename),
                () -> getWriteChannel(bucket, filename, contentType)
        );

        return blobFile;

    }

    public ReadableByteChannel getReadChannel(String bucket, String filename) {

        assertNotInTransaction();

        Path path = getLocalStoragePath(bucket, filename);

        if (!Files.exists(path)) {
            throw SneakyThrow.sneak(new FileNotFoundException("Not found: " + path));
        }

        try {
            SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ);
            return ChannelInvocationHandler.newChannelProxy(ReadableByteChannel.class, channel);
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    public WritableByteChannel getWriteChannel(String bucket, String filename, String contentType) {

        assertNotInTransaction();

        Path path = getLocalStoragePath(bucket, filename);

        try {
            Files.createDirectories(path.getParent());
            SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return ChannelInvocationHandler.newChannelProxy(WritableByteChannel.class, channel);
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    @Override
    public void copy(BlobFile source, BlobFile destination) {

        assertNotInTransaction();

        ReadableByteChannel sbc = source.reader();
        WritableByteChannel dbc = destination.writer();

        transfer(sbc, dbc, source.getSize());

    }

    @Override
    public void copy(BlobFile source, String destBucket, String destName) {

        assertNotInTransaction();

        ReadableByteChannel sbc = source.reader();
        WritableByteChannel dbc = getWriteChannel(destBucket, destName, source.getContentType());

        transfer(sbc, dbc, source.getSize());

    }

    private void transfer(ReadableByteChannel sbc, WritableByteChannel dbc, long length) {

        Channel src = unwrapProxyChannel(sbc);
        Channel dest = unwrapProxyChannel(dbc);

        if(src instanceof FileChannel) {
            try {
                ((FileChannel)src).transferTo(0, length, dbc);
            }
            catch(IOException ex) {
                throw SneakyThrow.sneak(ex);
            }
        }
        else if(dest instanceof FileChannel) {
            try {
                ((FileChannel)dest).transferFrom(sbc, 0, length);
            }
            catch(IOException ex) {
                throw SneakyThrow.sneak(ex);
            }
        }
        else {
            throw new IllegalStateException("Not FileChannels? channel classes= " + sbc.getClass().getName() + " " + dbc.getClass().getName());
        }

    }

    @Override
    public WritableByteChannel create(String bucketName, String filename) {
        return create(bucketName, filename, "application/octet-stream");
    }

    @Override
    public WritableByteChannel create(String bucketName, String filename, String mimeType) {
        assertNotInTransaction();
        return getWriteChannel(bucketName, filename, mimeType);
    }

    @Override
    public boolean delete(BlobFile blobFile) {

        assertNotInTransaction();

        Path path = getLocalStoragePath(blobFile.getBucket(), blobFile.getName());

        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    protected Path getLocalStoragePath(String bucket, String filename) {
        return Path.of(System.getProperty("user.home"), ".cloudstorage", bucket, filename);
    }

    private Channel unwrapProxyChannel(Channel channel) {

        if(!java.lang.reflect.Proxy.isProxyClass(channel.getClass())) {
            return channel;
        }

        ChannelInvocationHandler handler = (ChannelInvocationHandler)java.lang.reflect.Proxy.getInvocationHandler(channel);

        return handler.getChannel();

    }
}
