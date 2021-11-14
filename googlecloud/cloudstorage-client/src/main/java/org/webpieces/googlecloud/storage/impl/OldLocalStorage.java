package org.webpieces.googlecloud.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;

@Deprecated
@Singleton
public class OldLocalStorage implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(OldLocalStorage.class);

    private final ClientAssertions clientAssertions;
    private ChannelWrapper channelWrapperr;

    @Inject
    public OldLocalStorage(ClientAssertions clientAssertions, ChannelWrapper channelWrapperr) {
        this.clientAssertions = clientAssertions;
        this.channelWrapperr = channelWrapperr;
    }
//
//    @Override
//    public BlobFile getBlobFile(String bucket, String filename) {
//
//        clientAssertions.throwIfCannotGoRemote();
//
//        if (filename == null) {
//            throw new IllegalArgumentException("'filename' cannot be null!");
//        }
//
//        String contentType = "application/octet-stream";
//
//        InputStream in = getClass().getClassLoader().getResourceAsStream(bucket+"/"+filename);
//        if(in != null) {
//            throw new UnsupportedOperationException("please implement read from filesystem as needed and return here");
//        }
//
//        long size = 0;
//        Path localFile = getLocalStoragePath(bucket, filename);
//        if (Files.exists(localFile)) {
//            try {
//                size = Files.size(localFile);
//            } catch (IOException ex) {
//                throw SneakyThrow.sneak(ex);
//            }
//        }
//
//        BlobFile blobFile = new BlobFile(bucket, filename, contentType, size,
//                () -> getReadChannel(bucket, filename),
//                () -> getWriteChannel(bucket, filename, contentType)
//        );
//
//        return blobFile;
//
//    }
//
////    @Override
////    public URI getFileInZip(String bucket, String filename) {
////        //URI uri = URI.create("jar:file:" + Paths.get("").toAbsolutePath().toString() + "/build/resources/test/" + bucket + "/" + filename);
////        throw new UnsupportedOperationException("need to support from build/resources/test and build/cloudstorage");
////    }
//
//    public ReadableByteChannel getReadChannel(String bucket, String filename) {
//
//        clientAssertions.throwIfCannotGoRemote();
//
//        InputStream in = getClass().getClassLoader().getResourceAsStream(bucket+"/"+filename);
//        if(in != null) {
//            throw new UnsupportedOperationException("please implement read from filesystem as needed and return here");
//        }
//
//        Path path = getLocalStoragePath(bucket, filename);
//
//        if (!Files.exists(path)) {
//            throw SneakyThrow.sneak(new FileNotFoundException("Not found: " + path));
//        }
//
//        try {
//            SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ);
//            return channelWrapperr.newChannelProxy(ReadableByteChannel.class, channel);
//        } catch (IOException ex) {
//            throw SneakyThrow.sneak(ex);
//        }
//
//    }
//
//    public WritableByteChannel getWriteChannel(String bucket, String filename, String contentType) {
//
//        clientAssertions.throwIfCannotGoRemote();
//
//        Path path = getLocalStoragePath(bucket, filename);
//
//        try {
//            Files.createDirectories(path.getParent());
//            SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
//            return channelWrapperr.newChannelProxy(WritableByteChannel.class, channel);
//        } catch (IOException ex) {
//            throw SneakyThrow.sneak(ex);
//        }
//
//    }
//
//    @Override
//    public void copy(BlobFile source, BlobFile destination) {
//
//        clientAssertions.throwIfCannotGoRemote();
//
//        ReadableByteChannel sbc = source.reader();
//        WritableByteChannel dbc = destination.writer();
//
//        transfer(sbc, dbc, source.getSize());
//
//    }
//
//    @Override
//    public void copy(BlobFile source, String destBucket, String destName) {
//
//        clientAssertions.throwIfCannotGoRemote();
//
//        ReadableByteChannel sbc = source.reader();
//        WritableByteChannel dbc = getWriteChannel(destBucket, destName, source.getContentType());
//
//        transfer(sbc, dbc, source.getSize());
//
//    }
//
//    private void transfer(ReadableByteChannel sbc, WritableByteChannel dbc, long length) {
//
//        Channel src = unwrapProxyChannel(sbc);
//        Channel dest = unwrapProxyChannel(dbc);
//
//        if(src instanceof FileChannel) {
//            try {
//                ((FileChannel)src).transferTo(0, length, dbc);
//            }
//            catch(IOException ex) {
//                throw SneakyThrow.sneak(ex);
//            }
//        }
//        else if(dest instanceof FileChannel) {
//            try {
//                ((FileChannel)dest).transferFrom(sbc, 0, length);
//            }
//            catch(IOException ex) {
//                throw SneakyThrow.sneak(ex);
//            }
//        }
//        else {
//            throw new IllegalStateException("Not FileChannels? channel classes= " + sbc.getClass().getName() + " " + dbc.getClass().getName());
//        }
//
//    }
//
//    @Override
//    public WritableByteChannel create(String bucketName, String filename) {
//        return create(bucketName, filename, "application/octet-stream");
//    }
//
//    @Override
//    public WritableByteChannel create(String bucketName, String filename, String mimeType) {
//        clientAssertions.throwIfCannotGoRemote();
//        return getWriteChannel(bucketName, filename, mimeType);
//    }
//
//    @Override
//    public List<String> list(String bucket) {
//        //need to do full tree recursion here listing every file(every directory that is created?)
//        throw new UnsupportedOperationException("not supported yet");
//    }
//
//    @Override
//    public boolean delete(BlobFile blobFile) {
//
//        clientAssertions.throwIfCannotGoRemote();
//
//        Path path = getLocalStoragePath(blobFile.getBucket(), blobFile.getName());
//
//        try {
//            return Files.deleteIfExists(path);
//        } catch (IOException ex) {
//            throw SneakyThrow.sneak(ex);
//        }
//
//    }
//
//    protected Path getLocalStoragePath(String bucket, String filename) {
//        File f = new File("build/cloud-storage/"+bucket+"/"+filename);
//        return f.toPath();
//    }
//
//    private Channel unwrapProxyChannel(Channel channel) {
//
//        if(!java.lang.reflect.Proxy.isProxyClass(channel.getClass())) {
//            return channel;
//        }
//
//        ChannelInvocationHandler handler = (ChannelInvocationHandler)java.lang.reflect.Proxy.getInvocationHandler(channel);
//
//        return handler.getChannel();
//
//    }
}
