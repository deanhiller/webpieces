package org.webpieces.googlecloud.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class BlobFile {

    private final String bucket;
    private final String name;
    private final String contentType;
    private long size;
    private final Supplier<ReadableByteChannel> readableByteChannelSupplier;
    private final Supplier<WritableByteChannel> writableByteChannelSupplier;

    public BlobFile(String bucket, String name, String contentType, long size, Supplier<ReadableByteChannel> readableByteChannelSupplier,
                    Supplier<WritableByteChannel> writableByteChannelSupplier) {
        this.bucket = bucket;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.readableByteChannelSupplier = readableByteChannelSupplier;
        this.writableByteChannelSupplier = writableByteChannelSupplier;
    }

    public String getBucket() {
        return bucket;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    protected void setSize(long size) {
        this.size = size;
    }

    public BufferedReader bufferedReader() {
        return new BufferedReader(Channels.newReader(reader(), StandardCharsets.UTF_8), 512 * 1024);
    }

    public BufferedWriter bufferedWriter() {
        return new BufferedWriter(Channels.newWriter(writer(), StandardCharsets.UTF_8), 512 * 1024);
    }

    public ReadableByteChannel reader() {
        return ChannelInvocationHandler.newChannelProxy(ReadableByteChannel.class, readableByteChannelSupplier.get());
    }

    public WritableByteChannel writer() {
        return ChannelInvocationHandler.newChannelProxy(WritableByteChannel.class, writableByteChannelSupplier.get());
    }

}
