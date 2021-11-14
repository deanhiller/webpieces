package org.webpieces.googlecloud.storage.impl;

import org.webpieces.util.context.ClientAssertions;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.channels.Channel;

public class ChannelInvocationHandler implements InvocationHandler {

    private ClientAssertions clientAssertions;
    private Channel channel;

    @Inject
    public ChannelInvocationHandler(ClientAssertions clientAssertions) {
        this.clientAssertions = clientAssertions;
        this.channel = channel;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        clientAssertions.throwIfCannotGoRemote();

        return method.invoke(channel, args);

    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
