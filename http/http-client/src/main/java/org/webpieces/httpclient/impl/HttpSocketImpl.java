package org.webpieces.httpclient.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient.api.*;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;


public class HttpSocketImpl implements HttpSocket, Closeable {

    private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
    private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private TCPChannel channel;

    private CompletableFuture<RequestListener> connectFuture;
    private boolean isClosed;
    private boolean connected;
    private CloseListener closeListener;
    private HttpsSslEngineFactory factory;
    private ChannelManager mgr;
    private String idForLogging;
    private boolean isRecording = true;

    private InetSocketAddress addr;
    private ClientRequestListener requestListener;

    public HttpSocketImpl(ChannelManager mgr, String idForLogging, HttpsSslEngineFactory factory, HttpParser httpParser,
                          Http2Parser http2Parser,
                          CloseListener closeListener) {
        this.factory = factory;
        this.mgr = mgr;
        this.idForLogging = idForLogging;
        this.closeListener = closeListener;
        this.requestListener = new ClientRequestListener(this, httpParser, http2Parser, closeListener);
    }

    // HTTP Socket interface calls
    @Override
    public CompletableFuture<RequestListener> connect(InetSocketAddress addr) {
        if (factory == null) {
            channel = mgr.createTCPChannel(idForLogging);
        } else {
            SSLEngine engine = factory.createSslEngine(addr.getHostName(), addr.getPort());
            channel = mgr.createTCPChannel(idForLogging, engine);
        }
        requestListener.setChannel(channel);
        requestListener.setAddr(addr);

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
    public CompletableFuture<HttpResponse> send(HttpRequest request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        ResponseListener l = new CompletableListener(future);

        // This only works for complete requests
        if(connected) {
            requestListener.incomingRequest(request, true, l);
            return future;
        } else {
            throw new IllegalArgumentException("can't call 'send' until the socket is connected");
        }
    }

    @Override
    public CompletableFuture<HttpSocket> closeSocket() {
        if (isClosed) {
            return CompletableFuture.completedFuture(this);
        }
        requestListener.cleanUpPendings("close socket called");

        CompletableFuture<Channel> future = channel.close();
        return future.thenApply(chan -> {
            isClosed = true;
            return this;
        });
    }

    @Override
    public RequestListener getRequestListener() {
        return requestListener;
    }

    private synchronized RequestListener connected(InetSocketAddress addr) {
        connected = true;
        this.addr = addr;

        requestListener.clearPendingRequests();

        return requestListener;
    }

    @Override
    public void close() throws IOException {
        if(isClosed)
            return;

        //best effort and ignore exception except log it
        CompletableFuture<HttpSocket> future = closeSocket();
        future.exceptionally(e -> {
            log.info("close failed", e);
            return this;
        });
    }
}
