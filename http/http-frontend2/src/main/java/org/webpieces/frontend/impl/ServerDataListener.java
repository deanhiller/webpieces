package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;

import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

import com.webpieces.hpack.api.HpackParser;

class ServerDataListener implements AsyncDataListener {
    private HttpRequestListener timedRequestListener;
    private HttpParser httpParser;
    private HpackParser http2Parser;
    private FrontendConfig frontendConfig;

    ServerDataListener(HttpRequestListener timedRequestListener, HttpParser httpParser,
    		HpackParser http2Parser, FrontendConfig frontendConfig) {
        this.timedRequestListener = timedRequestListener;
        this.httpParser = httpParser;
        this.http2Parser = http2Parser;
        this.frontendConfig = frontendConfig;
    }

    @Override
    public void connectionOpened(TCPChannel tcpChannel, boolean isReadyForWrites) {
    }

    @Override
    public void incomingData(Channel channel, ByteBuffer b) {
    }

    @Override
    public void farEndClosed(Channel channel) {
    }

    @Override
    public void failure(Channel channel, ByteBuffer data, Exception e) {
    }

    @Override
    public void applyBackPressure(Channel channel) {
    }

    @Override
    public void releaseBackPressure(Channel channel) {
    }
}
