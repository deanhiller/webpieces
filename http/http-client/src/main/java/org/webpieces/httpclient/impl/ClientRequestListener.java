package org.webpieces.httpclient.impl;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.*;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.*;
import org.webpieces.httpclient.api.exceptions.*;
import org.webpieces.httpclient.api.exceptions.InternalError;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntUnaryOperator;

import static com.webpieces.http2parser.api.dto.Http2FrameType.HEADERS;
import static com.webpieces.http2parser.api.dto.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.*;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.SETTINGS_HEADER_TABLE_SIZE;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE;
import static java.lang.Math.min;
import static org.webpieces.httpclient.impl.ClientRequestListener.Protocol.HTTP11;
import static org.webpieces.httpclient.impl.ClientRequestListener.Protocol.HTTP2;
import static org.webpieces.httpclient.impl.Stream.StreamStatus.*;
import static org.webpieces.httpclient.impl.Stream.StreamStatus.CLOSED;

public class ClientRequestListener implements RequestListener {
    private static final Logger log = LoggerFactory.getLogger(ClientRequestListener.class);
    private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();


    private HttpSocket socket;
    enum Protocol { HTTP11, HTTP2 }
    private Protocol protocol = HTTP11;
    private ProxyDataListener dataListener;
    private CloseListener closeListener;
    private TCPChannel channel;
    private InetSocketAddress addr;

    private AtomicBoolean tryHttp2 = new AtomicBoolean(true);
    private AtomicBoolean negotiationDone = new AtomicBoolean(false);
    private AtomicBoolean negotiationStarted = new AtomicBoolean(false);
    private CompletableFuture<Channel> negotiationDoneNotifier = new CompletableFuture<>();
    private Http2Engine http2Engine;
    private Http2Parser http2Parser;

    // HTTP 1.1
    private HttpParser httpParser;
    private ConcurrentLinkedQueue<ResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();
    private AtomicBoolean acceptingRequest = new AtomicBoolean(false);


    public ClientRequestListener(HttpSocket socket,
                                 HttpParser httpParser,
                                 Http2Parser http2Parser,
                                 CloseListener closeListener,
                                 InetSocketAddress addr,
                                 TCPChannel channel) {
        this.socket = socket;
        this.httpParser = httpParser;
        this.http2Parser = http2Parser;
        this.closeListener = closeListener;
        this.http2Engine = new Http2Engine(http2Parser, channel, addr);
        this.channel = channel;
        this.addr = addr;

        dataListener = new ProxyDataListener();
        dataListener.put(HTTP2, this.http2Engine.getDataListener());
        dataListener.put(HTTP11, new Http11DataListener());
    }

    public DataListener getDataListener() {
        return dataListener;
    }

    public TCPChannel getChannel() {
        return channel;
    }

    public InetSocketAddress getAddr() {
        return addr;
    }

    private void enableHttp2() {
        protocol = HTTP2;
        dataListener.setProtocol(HTTP2);
        http2Engine.sendHttp2Preface();
        negotiationDone.set(true);

        // Initialize connection level flow control
        http2Engine.initialize();
    }

    // These hosts can support http2, no need to try to upgrade.
    private boolean defaultToHttp2(InetSocketAddress addr, TCPChannel channel) {
        return Arrays.asList("nghttp2.org").contains(addr.getHostName());

        // TODO: check channel for ALPN 'h2'
    }

    private CompletableFuture<RequestId> negotiateHttpVersion(HttpRequest req, boolean isComplete, ResponseListener listener) {
        // First check if ALPN says HTTP2, in which case, set the protocol to HTTP2 and we're done
        // If we set this to true, then everyting is copacetic with http://nghttp2.org, but
        // if we set it to false and do upgrade negotation, then the first request succeeds
        // but subsequent requests give us RstStream responses from the server.
        if (defaultToHttp2(addr, channel)) { // We don't know how to check ALPN yet, but if we do, put that check here
            log.info("setting http2 because of defaultToHttp2");
            enableHttp2();
            negotiationDone.set(true);
            negotiationDoneNotifier.complete(channel);
            return actuallySendRequest(req, isComplete, listener);

        } else { // Try the HTTP1.1 upgrade technique
            log.info("attempting http11 upgrade");
            req.addHeader(new Header("connection", "Upgrade, HTTP2-Settings"));
            req.addHeader(new Header("upgrade", "h2c"));
            Http2Settings settingsFrame = this.http2Engine.getLocalRequestedSettingsFrame();

            // For some reason we need to add a " " after the base64urlencoded settings to get this to work
            // against nghttp2.org ?
            req.addHeader(new Header("http2-settings",
                    Base64.getUrlEncoder().encodeToString(http2Parser.marshal(settingsFrame).createByteArray()) + " "));

            CompletableFuture<HttpResponse> response = sendHttp11AndWaitForHeaders(req);

            return response.thenApply(r -> {
                if(r.getStatusLine().getStatus().getCode() != 101) {
                    log.info("upgrade failed");
                    // That didn't work, let's not try http2 and send what we have so far to the normal listener
                    tryHttp2.set(false);
                    negotiationDone.set(true);
                    negotiationDoneNotifier.complete(channel);

                    // If the response is chunked then it is probably not complete.
                    // TODO: make sure this is right. would be nicer to grab the isComplete
                    // out of the incomingResponse call to the CompletableListener in
                    // sendHttp11AndWaitForHeaders I think.
                    listener.incomingResponse(r, !r.isHasChunkedTransferHeader());
                    // Request id is 0 for HTTP/1.1
                    return new RequestId(0);
                } else {
                    log.info("upgrade succeeded");
                    enableHttp2();

                    // Grab the leftover data out of the http11 parser and send that
                    // to the http2 engine
                    DataWrapper leftOverData = ((ClientRequestListener.Http11DataListener) dataListener.dataListenerMap.get(HTTP11))
                            .getLeftOverData();
                    return http2Engine.createInitialStream(r, req, listener, leftOverData);
                }
            });
        }
    }

    private CompletableFuture<HttpResponse> sendHttp11AndWaitForHeaders(HttpRequest request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
        ResponseListener l = new CompletableListener(future, true);
        // This only works for complete requests
        sendHttp11Request(request, true, l);
        return future;
    }

    @Override
    public CompletableFuture<RequestId> incomingRequest(HttpRequest request, boolean isComplete, ResponseListener listener) {
        if(acceptingRequest.get()) {
            throw new IllegalArgumentException("You can't call incoming request while in "
                    + "HTTP11 mode and a prior request is not complete");
        }

        if(!isComplete && protocol == HTTP11) {
            acceptingRequest.set(true);
        }

        return negotiateAndSendRequest(request, isComplete, listener);
    }

    @Override
    public CompletableFuture<Integer> incomingData(RequestId id, DataWrapper data, boolean isComplete) {
        if(protocol == HTTP11) {
            if(isComplete)
                acceptingRequest.set(false);

            // TODO: create a chunk out of the data
            throw new IllegalArgumentException("incomingData not implemented for HTTP/1.1");
        }
        else {
            return http2Engine.incomingData(id, data, isComplete);
        }
    }
    private CompletableFuture<RequestId> sendHttp11Request(HttpRequest request, boolean isComplete, ResponseListener l) {
        ByteBuffer wrap = ByteBuffer.wrap(httpParser.marshalToBytes(request));

        // TODO: confirm that is isComplete is false that the transfer-encoding is chunked, otherwise throw
        if(!isComplete)
            throw new IllegalArgumentException("can only send complete requests for HTTP1.1 right now");

        //put this on the queue before the write to be completed from the listener below
        responsesToComplete.offer(l);

        log.info("sending request now. req=" + request);

        // HTTP/1.1 has request ids of 0
        return channel.write(wrap).thenApply(channel -> new RequestId(0));
    }

    private CompletableFuture<RequestId> negotiateAndSendRequest(HttpRequest request, boolean isComplete, ResponseListener listener) {
        ResponseListener l = new CatchResponseListener(listener);
        if (!negotiationDone.get()) {
            if (!negotiationStarted.get()) {
                negotiationStarted.set(true);
                return negotiateHttpVersion(request, isComplete, l);
            } else {
                log.info("waiting for negotiation to complete");
                return negotiationDoneNotifier.thenCompose(channel -> {
                    log.info("done waiting for negotiation to complete");
                    return actuallySendRequest(request, isComplete, l);
                });
            }
        } else {
            log.info("not waiting for negotiation at all");
            return actuallySendRequest(request, isComplete, l);
        }
    }

    private CompletableFuture<RequestId> actuallySendRequest(HttpRequest request, boolean isComplete, ResponseListener l) {
        if (protocol == HTTP11) {
            return sendHttp11Request(request, isComplete, l);
        } else { // HTTP2
            return http2Engine.sendHttp2Request(request, isComplete, l);
        }
    }

    public void cleanUpPendings(String msg) {
        //do we need an isClosing state and cache that future?  (I don't think so but time will tell)
        while(!responsesToComplete.isEmpty()) {
            ResponseListener listener = responsesToComplete.poll();
            if(listener != null) {
                listener.failure(new NioClosedChannelException(msg+" before responses were received"));
            }
        }

        // TODO: Deal with open streams
        http2Engine.cleanUpPendings(msg);
    }

    private class ProxyDataListener implements DataListener {
        private Protocol protocol = HTTP11;
        private Map<Protocol, DataListener> dataListenerMap = new HashMap<>();

        void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public void put(Protocol protocol, DataListener listener) {
            dataListenerMap.put(protocol, listener);
        }

        @Override
        public void incomingData(Channel channel, ByteBuffer b) {
            dataListenerMap.get(protocol).incomingData(channel, b);
        }

        @Override
        public void farEndClosed(Channel channel) {
            log.info("far end closed");
            socket.closeSocket();

            if(closeListener != null)
                closeListener.farEndClosed(socket);

            // call farEndClosed on every protocol
            for(Map.Entry<Protocol, DataListener> entry: dataListenerMap.entrySet()) {
                entry.getValue().farEndClosed(channel);
            }
        }

        @Override
        public void failure(Channel channel, ByteBuffer data, Exception e) {
            log.error("Failure on channel="+channel, e);

            // Call failure on every protocol
            for(Map.Entry<Protocol, DataListener> entry: dataListenerMap.entrySet()) {
                entry.getValue().failure(channel, data, e);
            }
        }

        @Override
        public void applyBackPressure(Channel channel) {
            dataListenerMap.get(protocol).applyBackPressure(channel);
        }

        @Override
        public void releaseBackPressure(Channel channel) {
            dataListenerMap.get(protocol).releaseBackPressure(channel);
        }
    }

    private class Http11DataListener implements DataListener {
        private boolean processingChunked = false;
        private Memento memento = httpParser.prepareToParse();

        /**
         * This is a special 'reach-in' method to let the http2 parser grab the data from the http11
         * parser that has not yet been parsed.
         *
         * @return
         */
        public DataWrapper getLeftOverData() {
            return memento.getLeftOverData();
        }

        @Override
        public void incomingData(Channel channel, ByteBuffer b) {
            log.info("size="+b.remaining());
            DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
            memento = httpParser.parse(memento, wrapper);

            List<HttpPayload> parsedMessages = memento.getParsedMessages();
            for(HttpPayload msg : parsedMessages) {
                if(processingChunked) {
                    HttpChunk chunk = (HttpChunk) msg;
                    ResponseListener listener = responsesToComplete.peek();
                    if(chunk.isLastChunk()) {
                        processingChunked = false;
                        responsesToComplete.poll();
                    }

                    listener.incomingData(chunk.getBodyNonNull(), chunk.isLastChunk());
                } else if(!msg.isHasChunkedTransferHeader()) {
                    HttpResponse resp = (HttpResponse) msg;
                    ResponseListener listener = responsesToComplete.poll();
                    listener.incomingResponse(resp, true);
                } else {
                    processingChunked = true;
                    HttpResponse resp = (HttpResponse) msg;
                    ResponseListener listener = responsesToComplete.peek();
                    listener.incomingResponse(resp, false);
                }
            }
        }

        @Override
        public void farEndClosed(Channel channel) {
            cleanUpPendings("Remote end closed");
        }

        @Override
        public void failure(Channel channel, ByteBuffer data, Exception e) {
            while(!responsesToComplete.isEmpty()) {
                ResponseListener listener = responsesToComplete.poll();
                if(listener != null) {
                    listener.failure(e);
                }
            }
        }

        @Override
        public void applyBackPressure(Channel channel) {

        }

        @Override
        public void releaseBackPressure(Channel channel) {
        }
    }

}
