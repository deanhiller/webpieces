package org.webpieces.aws.storage.impl;

import org.webpieces.aws.storage.api.AWSBlob;
import org.webpieces.aws.storage.api.AWSRawStorage;
import org.webpieces.aws.storage.api.AWSStorage;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.stream.Stream;

/**
 * Since tests mock rawStorage, changes to this class get included in testing.
 * THIS IS A GOOD THING ^^^^.   Do not break people
 */
@Singleton
public class AWSStorageImpl implements AWSStorage {

    private AWSRawStorage rawStorage;
    private ClientAssertions clientAssertions;
    private ChannelWrapper channelWrapper;

    @Inject
    public AWSStorageImpl(AWSRawStorage rawStorage, ClientAssertions clientAssertions, ChannelWrapper channelWrapper) {
        this.rawStorage = rawStorage;
        this.clientAssertions = clientAssertions;
        this.channelWrapper = channelWrapper;
    }

    @Override
    public AWSBlob get(String bucket, String key) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.get(bucket, key);
    }

    @Override
    public Stream<AWSBlob> list(String bucket) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.list(bucket);
    }

    @Override
    public boolean delete(String bucket, String key) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.delete(bucket, key);
    }

    @Override
    public byte[] readAllBytes(String bucket, String key) {
        clientAssertions.throwIfCannotGoRemote();
        return rawStorage.readAllBytes(bucket, key);
    }

    @Override
    public ReadableByteChannel reader(String bucket, String key) {
        clientAssertions.throwIfCannotGoRemote();
        return channelWrapper.newChannelProxy(ReadableByteChannel.class, rawStorage.reader(bucket, key));
    }

    @Override
    public WritableByteChannel writer(String bucket, String key) {
        clientAssertions.throwIfCannotGoRemote();
        return channelWrapper.newChannelProxy(WritableByteChannel.class, rawStorage.writer(bucket, key));
    }

    @Override
    public boolean copy(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        clientAssertions.throwIfCannotGoRemote();
        //return null;
        return rawStorage.copy(sourceBucket, sourceKey, destBucket, destKey);
    }
}
