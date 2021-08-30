package org.webpieces.googlecloud.storage.impl;

import com.google.cloud.storage.Blob;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Proxy;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ChannelWrapper {

    private Provider<ChannelInvocationHandler> invocHandlerProvider;

    @Inject
    public ChannelWrapper(Provider<ChannelInvocationHandler> invocHandlerProvider) {
        this.invocHandlerProvider = invocHandlerProvider;
    }

    public ReadableByteChannel createReader(Blob blob) {
        return newChannelProxy(ReadableByteChannel.class, blob.reader());
    }

    public WritableByteChannel createWriter(Blob blob) {
        //any code in here is a delayed call
        return newChannelProxy(WritableByteChannel.class, blob.writer());
    }

    public <T extends Channel> T newChannelProxy(Class<T> intf, T channel) {
        ChannelInvocationHandler invocHandler = invocHandlerProvider.get();
        invocHandler.setChannel(channel);

        return (T) Proxy.newProxyInstance(channel.getClass().getClassLoader(),
                new Class[] {intf, Channel.class},
                invocHandler);
    }



}
