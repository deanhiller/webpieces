package org.webpieces.frontend.impl;

import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

import java.nio.ByteBuffer;

public class ServerDataListener implements AsyncDataListener {
    private TimedListener timedListener;
    private Http11DataListener http11DataListener;
    private HttpParser httpParser;

    private HttpServerSocket getHttpServerSocketForChannel(Channel channel) {
        ChannelSession session = channel.getSession();
        HttpServerSocket httpServerSocket = (HttpServerSocket) session.get("webpieces.httpServerSocket");
        if(httpServerSocket == null) {
            httpServerSocket = new HttpServerSocketImpl(
                    channel,
                    http11DataListener,
                    new Http11ResponseSender(channel, httpParser));
            session.put("webpieces.httpServerSocket", httpServerSocket);
        }
        return httpServerSocket;
    }

    public ServerDataListener(TimedListener timedListener, Http11DataListener http11DataListener, HttpParser httpParser) {
        this.timedListener = timedListener;
        this.http11DataListener = http11DataListener;
        this.httpParser = httpParser;
    }

    @Override
    public void connectionOpened(TCPChannel tcpChannel, boolean isReadyForWrites) {
        // Create the HttpSocket here if one doesn't exist already.

        timedListener.openedConnection(getHttpServerSocketForChannel(tcpChannel), isReadyForWrites);
    }

    @Override
    public void incomingData(Channel channel, ByteBuffer b) {
        getHttpServerSocketForChannel(channel).getDataListener().incomingData(channel, b);
    }

    @Override
    public void farEndClosed(Channel channel) {
        getHttpServerSocketForChannel(channel).getDataListener().farEndClosed(channel);

    }

    @Override
    public void failure(Channel channel, ByteBuffer data, Exception e) {
        getHttpServerSocketForChannel(channel).getDataListener().failure(channel, data, e);
    }

    @Override
    public void applyBackPressure(Channel channel) {
        getHttpServerSocketForChannel(channel).getDataListener().applyBackPressure(channel);
    }

    @Override
    public void releaseBackPressure(Channel channel) {
        getHttpServerSocketForChannel(channel).getDataListener().releaseBackPressure(channel);
    }
}
