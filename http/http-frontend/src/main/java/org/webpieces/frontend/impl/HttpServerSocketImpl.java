package org.webpieces.frontend.impl;

import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

import java.util.concurrent.CompletableFuture;


public class HttpServerSocketImpl implements HttpServerSocket {
    private Channel channel;
    private DataListener dataListener;
    private ResponseSender responseSender;

    public HttpServerSocketImpl(Channel channel, DataListener dataListener, ResponseSender responseSender) {
        this.channel = channel;
        this.dataListener = dataListener;
        this.responseSender = responseSender;
    }

    @Override
    public CompletableFuture<Void> closeSocket() {
        return null;
    }

    @Override
    public Channel getUnderlyingChannel() {
        return channel;
    }

    @Override
    public ResponseSender getResponseSender() {
        return responseSender;
    }

    @Override
    public DataListener getDataListener() {
        return dataListener;
    }
}
