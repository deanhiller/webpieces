package org.webpieces.frontend.impl;

import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.Http2Engine;
import org.webpieces.httpcommon.api.Http2EngineFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

import java.nio.ByteBuffer;
import java.util.Optional;

import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.SERVER;

public class ServerDataListener implements AsyncDataListener {
    private TimedListener timedListener;
    private Http11DataListener http11DataListener;
    private HttpParser httpParser;
    private Http2Parser http2Parser;

    private HttpServerSocket getHttpServerSocketForChannel(Channel channel) {
        ChannelSession session = channel.getSession();
        HttpServerSocket httpServerSocket = (HttpServerSocket) session.get("webpieces.httpServerSocket");
        if(httpServerSocket == null) {
            // Default to HTTP/1.1 but pass in the parser so that we can upgrade to http2.
            httpServerSocket = new HttpServerSocketImpl(
                    channel,
                    http11DataListener,
                    new Http11ResponseSender(channel, httpParser),
                    http2Parser,
                    timedListener);

            session.put("webpieces.httpServerSocket", httpServerSocket);
        }
        return httpServerSocket;
    }

    public ServerDataListener(TimedListener timedListener, Http11DataListener http11DataListener, HttpParser httpParser,
                              Http2Parser http2Parser) {
        this.timedListener = timedListener;
        this.http11DataListener = http11DataListener;
        this.httpParser = httpParser;
        this.http2Parser = http2Parser;
    }

    @Override
    public void connectionOpened(TCPChannel tcpChannel, boolean isReadyForWrites) {
        HttpServerSocket socket = getHttpServerSocketForChannel(tcpChannel);
        // TODO: replace 'false' with ALPN check
        if(isReadyForWrites && tcpChannel.isSslChannel() && false) { // If ALPN, upgrade to HTTP2
            socket.upgradeHttp2(Optional.empty());
        }
        timedListener.openedConnection(socket, isReadyForWrites);
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
