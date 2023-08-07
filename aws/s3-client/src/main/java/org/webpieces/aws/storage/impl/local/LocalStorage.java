package org.webpieces.aws.storage.impl.local;


import org.webpieces.aws.storage.api.AWSBlob;
import org.webpieces.aws.storage.api.AWSRawStorage;
import org.webpieces.aws.storage.impl.raw.AWSBlobImpl;
import org.webpieces.util.SneakyThrow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

@Singleton
public class LocalStorage implements AWSRawStorage {
    public static final String LOCAL_BUILD_DIR = "build/local-s3storage/";

    @Inject
    public LocalStorage() {
    }

    @Override
    public AWSBlob get(String bucket, String key) {

        InputStream in = getInputStream(bucket, key);
        if(in != null) {
            return new LocalAWSBlobImpl(bucket, key, null, -1);
        }

        Path path = getFilePath(bucket, key);

        if(Files.exists(path)) {
            long size = -1;
            try {
                size = Files.size(path);
            } catch(IOException ex) {
                throw SneakyThrow.sneak(ex);
            }
            return new LocalAWSBlobImpl(bucket, key, null, size);
        }

        return null;
    }

    @Override
    public Stream<AWSBlob> list(String bucket) {

        Path bucketPath = getBucketPath(bucket);

        try {

            if(!Files.exists(bucketPath)) {
                Files.createDirectories(bucketPath);
            }

            return Files.find(bucketPath, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).map(p -> {
                long size = -1;
                try {
                    size = Files.size(p);
                } catch (IOException ex) {
                    throw SneakyThrow.sneak(ex);
                }
                return new AWSBlobImpl(bucket, bucketPath.relativize(p).toString(), null, size);
            });

        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    @Override
    public boolean delete(String bucket, String key) {

        Path file = getFilePath(bucket, key);

        try {
            return Files.deleteIfExists(file);
        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    @Override
    public byte[] readAllBytes(String bucket, String key) {

        Path file = getFilePath(bucket, key);

        if(!Files.exists(file)) {
            return null;
        }

        try {
            return Files.readAllBytes(file);
        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    @Override
    public ReadableByteChannel reader(String bucket, String key) {

        InputStream in = getInputStream(bucket, key);
        if(in != null) {
            ReadableByteChannel channel = Channels.newChannel(in);
            return channel;
        }

        Path path = getFilePath(bucket, key);

        if(!Files.exists(path)) {
            return null;
        }

        try {
            return FileChannel.open(path, StandardOpenOption.READ);
        }
        catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    @Override
    public WritableByteChannel writer(String bucket, String key) {

        Path path = getFilePath(bucket, key);

        try {
            Files.createDirectories(path.getParent());
        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

        try {
            return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    @Override
    public boolean copy(String sourceBucket, String sourceKey, String destBucket, String destKey) {

        ReadableByteChannel source = reader(sourceBucket, sourceKey);
        Path dest = getFilePath(destBucket, destKey);

        try {
            Files.createDirectories(dest.getParent());
        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

        try(FileChannel fc = FileChannel.open(dest, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            fc.transferFrom(source, 0, Long.MAX_VALUE);
        } catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

        return true;

    }

    private Path getBucketPath(String bucket) {
        return Path.of(LOCAL_BUILD_DIR, bucket);
    }

    private Path getFilePath(String bucket, String key) {
        return getBucketPath(bucket).resolve(key);
    }

    private InputStream getInputStream(String bucket, String key) {
        return this.getClass().getClassLoader().getResourceAsStream(bucket + "/" + key);
    }

}
