package org.webpieces.googlecloud.storage.impl;

import org.webpieces.util.context.ClientAssertions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.channels.Channel;

public class ChannelInvocationHandler implements InvocationHandler {

    private ClientAssertions clientAssertions;
    private final Channel channel;

    public ChannelInvocationHandler(ClientAssertions clientAssertions, Channel channel) {
        this.clientAssertions = clientAssertions;
        this.channel = channel;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        clientAssertions.throwIfCannotGoRemote();

        return method.invoke(channel, args);

    }

    public Channel getChannel() {
        return channel;
    }

}
