package org.webpieces.httpclient.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpclient.api.HttpsSslEngineFactory;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ServerListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;


public class HttpClientSocketImpl implements HttpClientSocket, Closeable {

    private static final Logger log = LoggerFactory.getLogger(HttpClientSocketImpl.class);

    private TCPChannel channel;
    private HttpParser httpParser;
    private Http2Parser http2Parser;
    private Http2SettingsMap http2SettingsMap;

    private CompletableFuture<RequestSender> connectFuture;
    private boolean isClosed;
    private ServerListener closeListener;
    private HttpsSslEngineFactory factory;
    private ChannelManager mgr;
    private String idForLogging;
    private boolean isRecording = false;

    private RequestSenderImpl requestSender;

    public HttpClientSocketImpl(
        ChannelManager mgr,
        String idForLogging,
        HttpsSslEngineFactory factory,
        HttpParser httpParser,
        Http2Parser http2Parser,
        ServerListener closeListener,
        Http2SettingsMap http2SettingsMap)
    {
        this.factory = factory;
        this.mgr = mgr;
        this.idForLogging = idForLogging;
        this.closeListener = closeListener;
        this.http2Parser = http2Parser;
        this.httpParser = httpParser;
        this.http2SettingsMap = http2SettingsMap;
    }

    @Override
    public TCPChannel getUnderlyingChannel() {
        return channel;
    }

    // HTTP Socket interface calls
    @Override
    public CompletableFuture<RequestSender> connect(InetSocketAddress addr) {
        if (factory == null) {
            channel = mgr.createTCPChannel(idForLogging);
        } else {
            SSLEngine engine = factory.createSslEngine(addr.getHostName(), addr.getPort());
            channel = mgr.createTCPChannel(idForLogging, engine);
        }

        requestSender = new RequestSenderImpl(
            this,
            this.httpParser,
            this.http2Parser,
            closeListener,
            addr,
            channel,
            http2SettingsMap
            );
        DataListener dataListener;

        if (isRecording) {
            dataListener = new RecordingDataListener("httpSock-", requestSender.getDataListener());
        } else {
            dataListener = requestSender.getDataListener();
        }

        connectFuture = channel.connect(addr, dataListener).thenApply(channel -> requestSender);
        return connectFuture;
    }

    @Override
    public CompletableFuture<Void> closeSocket() {
        if (isClosed) {
            return CompletableFuture.completedFuture(null);
        }
        requestSender.cleanUpPendings("close socket called");

        CompletableFuture<Channel> future = channel.close();
        return future.thenAccept(chan -> {
            isClosed = true;
        });
    }

    @Override
    public RequestSender getRequestSender() {
        return requestSender;
    }

    @Override
    public void close() throws IOException {
        if(isClosed)
            return;

        //best effort and ignore exception except log it
        CompletableFuture<Void> future = closeSocket();
        future.exceptionally(e -> {
            log.info("close failed", e);
            return null;
        });
    }

	@Override
	public String toString() {
		return "HttpClientSocket["+idForLogging+", channel="+channel+"]";
	}
    
}
