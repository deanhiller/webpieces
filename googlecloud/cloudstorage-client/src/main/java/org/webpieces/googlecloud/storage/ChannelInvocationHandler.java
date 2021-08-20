package org.webpieces.googlecloud.storage;

import org.webpieces.plugin.hibernate.Em;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.channels.Channel;

public class ChannelInvocationHandler implements InvocationHandler {

    private final Channel channel;

    public ChannelInvocationHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        EntityManager em = Em.get();
        if (em != null) {
            throw new IllegalStateException("You should never make remote calls or read/write to streams while in a transaction");
        }

        return method.invoke(channel, args);

    }

    public static <T extends Channel> T newChannelProxy(Class<T> intf, T channel) {
        return (T) Proxy.newProxyInstance(channel.getClass().getClassLoader(),
                new Class[] {intf, Channel.class},
                new ChannelInvocationHandler(channel));
    }

    public Channel getChannel() {
        return channel;
    }

}
