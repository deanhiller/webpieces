package org.webpieces.frontend.impl;

import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

import java.nio.ByteBuffer;
import java.util.Optional;

public class ServerDataListener implements AsyncDataListener {
    private TimedRequestListener timedRequestListener;
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
                    timedRequestListener);

            session.put("webpieces.httpServerSocket", httpServerSocket);
        }
        return httpServerSocket;
    }

    public ServerDataListener(TimedRequestListener timedRequestListener, Http11DataListener http11DataListener, HttpParser httpParser,
                              Http2Parser http2Parser) {
        this.timedRequestListener = timedRequestListener;
        this.http11DataListener = http11DataListener;
        this.httpParser = httpParser;
        this.http2Parser = http2Parser;
    }

    @Override
    public void connectionOpened(TCPChannel tcpChannel, boolean isReadyForWrites) {
        HttpServerSocket socket = getHttpServerSocketForChannel(tcpChannel);
        // TODO: replace 'false' with ALPN check
        if(isReadyForWrites && tcpChannel.isSslChannel() && false) { // If ALPN, upgrade to HTTP2
        // if we want h2spec to work we just need to enable this if(true) here.
        // we could either make this configurable or make it so that if the http11
        // code sees the HTTP/2 preface then it switches to HTTP/2 but since we
        // are going to have ALPN "soon" I don't think it is worth it.
        //if(true) {
            socket.upgradeHttp2(Optional.empty());
            //socket.sendLocalPreferredSettings(Optional.empty());
        }
        timedRequestListener.openedConnection(socket, isReadyForWrites);
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
