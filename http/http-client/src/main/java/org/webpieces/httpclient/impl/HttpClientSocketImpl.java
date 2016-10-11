package org.webpieces.httpclient.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient.api.*;
import org.webpieces.httpcommon.api.CloseListener;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;


public class HttpClientSocketImpl implements HttpClientSocket, Closeable {

    private static final Logger log = LoggerFactory.getLogger(HttpClientSocketImpl.class);
    private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private TCPChannel channel;
    private HttpParser httpParser;
    private Http2Parser http2Parser;

    private CompletableFuture<RequestSender> connectFuture;
    private boolean isClosed;
    private boolean connected;
    private CloseListener closeListener;
    private HttpsSslEngineFactory factory;
    private ChannelManager mgr;
    private String idForLogging;
    private boolean isRecording = true;

    private InetSocketAddress addr;
    private RequestSenderImpl requestListener;

    public HttpClientSocketImpl(ChannelManager mgr, String idForLogging, HttpsSslEngineFactory factory, HttpParser httpParser,
                                Http2Parser http2Parser,
                                CloseListener closeListener) {
        this.factory = factory;
        this.mgr = mgr;
        this.idForLogging = idForLogging;
        this.closeListener = closeListener;
        this.http2Parser = http2Parser;
        this.httpParser = httpParser;
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

        requestListener = new RequestSenderImpl(this,
                this.httpParser,
                this.http2Parser,
                closeListener,
                addr,
                channel);
        DataListener dataListener;

        if (isRecording) {
            dataListener = new RecordingDataListener("httpSock-", requestListener.getDataListener());
        } else {
            dataListener = requestListener.getDataListener();
        }

        connectFuture = channel.connect(addr, dataListener).thenApply(channel -> connected(addr));
        return connectFuture;
    }

    @Override
    public CompletableFuture<Void> closeSocket() {
        if (isClosed) {
            return CompletableFuture.completedFuture(null);
        }
        requestListener.cleanUpPendings("close socket called");

        CompletableFuture<Channel> future = channel.close();
        return future.thenAccept(chan -> {
            isClosed = true;
        });
    }

    @Override
    public RequestSender getRequestSender() {
        return requestListener;
    }

    private synchronized RequestSender connected(InetSocketAddress addr) {
        connected = true;
        this.addr = addr;

        return requestListener;
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
}
