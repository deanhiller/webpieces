package org.webpieces.aws.storage.impl;

import java.lang.reflect.Proxy;
import java.nio.channels.Channel;
import javax.inject.Inject;
import javax.inject.Provider;

public class ChannelWrapper {

    private Provider<ChannelInvocationHandler> invocHandlerProvider;

    @Inject
    public ChannelWrapper(Provider<ChannelInvocationHandler> invocHandlerProvider) {
        this.invocHandlerProvider = invocHandlerProvider;
    }

    public <T extends Channel> T newChannelProxy(Class<T> intf, T channel) {
        ChannelInvocationHandler invocHandler = invocHandlerProvider.get();
        invocHandler.setChannel(channel);

        return (T) Proxy.newProxyInstance(channel.getClass().getClassLoader(),
                new Class[] {intf, Channel.class},
                invocHandler);
    }



}
