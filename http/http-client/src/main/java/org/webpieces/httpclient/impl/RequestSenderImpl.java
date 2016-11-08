package org.webpieces.httpclient.impl;

import static org.webpieces.httpcommon.api.Protocol.HTTP11;
import static org.webpieces.httpcommon.api.Protocol.HTTP2;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.api.*;
import org.webpieces.httpcommon.api.SwitchableDataListenerFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.Http2Settings;

public class RequestSenderImpl implements RequestSender {
    private static final Logger log = LoggerFactory.getLogger(RequestSenderImpl.class);
    private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private Protocol protocol = HTTP11;
    private SwitchableDataListener dataListener;
    private TCPChannel channel;
    private InetSocketAddress addr;

    private AtomicBoolean tryHttp2 = new AtomicBoolean(true);
    private AtomicBoolean negotiationDone = new AtomicBoolean(false);
    private AtomicBoolean negotiationStarted = new AtomicBoolean(false);
    private CompletableFuture<Channel> negotiationDoneNotifier = new CompletableFuture<>();
    private Http2ClientEngine http2ClientEngine;
    private Http2Parser http2Parser;

    // HTTP 1.1
    private HttpParser httpParser;
    private class RequestAwaitingCompletion {
        ResponseListener listener;
        HttpRequest request;

        public RequestAwaitingCompletion(ResponseListener listener, HttpRequest request) {
            this.listener = listener;
            this.request = request;
        }
    }
    private ConcurrentLinkedQueue<RequestAwaitingCompletion> responsesToComplete = new ConcurrentLinkedQueue<>();
    private AtomicBoolean acceptingRequest = new AtomicBoolean(false);


    public RequestSenderImpl(
        HttpClientSocket socket,
        HttpParser httpParser,
        Http2Parser http2Parser,
        CloseListener closeListener,
        InetSocketAddress addr,
        TCPChannel channel,
        Http2SettingsMap http2SettingsMap
    ) {
        this.httpParser = httpParser;
        this.http2Parser = http2Parser;
        this.http2ClientEngine = Http2EngineFactory.createHttp2ClientEngine(http2Parser, channel, addr, http2SettingsMap);
        this.channel = channel;
        this.addr = addr;

        dataListener = SwitchableDataListenerFactory.createSwitchableDataListener(socket, closeListener);
        dataListener.put(HTTP2, this.http2ClientEngine.getDataListener());
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
        http2ClientEngine.sendHttp2Preface();
        http2ClientEngine.sendLocalRequestedSettings();
        negotiationDone.set(true);

        // Initialize connection level flow control
        http2ClientEngine.startPing();
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
            req.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
            req.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
            Http2Settings settingsFrame = this.http2ClientEngine.getLocalRequestedSettingsFrame();

            // For some reason we need to add a " " after the base64urlencoded settings to get this to work
            // against nghttp2.org ?
            // TODO: check if we still need this " " now that we are only shipping the payload and not the
            // whole frame
            byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

            // strip the header
            byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
            req.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                    Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

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
                    // sendHttp11AndWaitForHeaders I think. We don't really support
                    // chunked requests yet anyway, so.
                    listener.incomingResponse(r, req, new ResponseId(0), !r.isHasChunkedTransferHeader());
                    // Request id is 0 for HTTP/1.1
                    return new RequestId(0);
                } else {
                    log.info("upgrade succeeded");
                    enableHttp2();

                    // Grab the leftover data out of the http11 parser and send that
                    // to the http2 engine
                    DataWrapper leftOverData = ((RequestSenderImpl.Http11DataListener) dataListener.getDataListener(HTTP11))
                            .getLeftOverData();
                    return http2ClientEngine.createInitialStream(r, req, listener, leftOverData);
                }
            });
        }
    }

    private CompletableFuture<HttpResponse> sendHttp11AndWaitForHeaders(HttpRequest request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        ResponseListener l = new CompletableListener(future, true);
        // This only works for complete requests
        sendHttp11Request(request, true, l);
        return future;
    }


    @Override
    public CompletableFuture<HttpResponse> send(HttpRequest request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        ResponseListener l = new CompletableListener(future);
        sendRequest(request, true, l);
        return future;
    }

    @Override
    public CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener listener) {
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
    public CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete) {
        if(protocol == HTTP11) {
            if(isComplete)
                acceptingRequest.set(false);

            // TODO: create a chunk out of the data
            throw new UnsupportedOperationException("sendData not implemented for HTTP/1.1");
        }
        else {
            return http2ClientEngine.sendData(id, data, isComplete);
        }
    }

    @Override
    public void sendTrailer(List<HasHeaderFragment.Header> headers, RequestId id, boolean isComplete) {
        if(protocol == HTTP11) {
            if(isComplete)
                acceptingRequest.set(false);
        }
        throw new UnsupportedOperationException("sendTrailer not implemented");
    }

    @Override
    public void failure(Throwable e) {
        // TODO: fill this in appropriately
        throw new UnsupportedOperationException();
    }

    private CompletableFuture<RequestId> sendHttp11Request(HttpRequest request, boolean isComplete, ResponseListener l) {
        ByteBuffer wrap = ByteBuffer.wrap(httpParser.marshalToBytes(request));

        // TODO: confirm that if isComplete is false that the transfer-encoding is chunked, otherwise throw
        if(!isComplete)
            throw new IllegalArgumentException("can only send complete requests for HTTP1.1 right now");

        //put this on the queue before the write to be completed from the listener below
        responsesToComplete.offer(new RequestAwaitingCompletion(l, request));

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
            return http2ClientEngine.sendRequest(request, isComplete, l);
        }
    }

    public void cleanUpPendings(String msg) {
        //do we need an isClosing state and cache that future?  (I don't think so but time will tell)
        while(!responsesToComplete.isEmpty()) {
            ResponseListener listener = responsesToComplete.poll().listener;
            if(listener != null) {
                listener.failure(new NioClosedChannelException(msg+" before responses were received"));
            }
        }

        // TODO: Deal with open streams
        http2ClientEngine.cleanUpPendings(msg);
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
            log.info("http11 incomingData -> size="+b.remaining());
            DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
            memento = httpParser.parse(memento, wrapper);

            List<HttpPayload> parsedMessages = memento.getParsedMessages();
            for(HttpPayload msg : parsedMessages) {
                if(processingChunked) {
                    HttpChunk chunk = (HttpChunk) msg;
                    ResponseListener listener = responsesToComplete.peek().listener;
                    if(chunk.isLastChunk()) {
                        processingChunked = false;
                        responsesToComplete.poll();
                    }

                    listener.incomingData(chunk.getBodyNonNull(), new ResponseId(0), chunk.isLastChunk());
                } else if(!msg.isHasChunkedTransferHeader()) {
                    HttpResponse resp = (HttpResponse) msg;
                    RequestAwaitingCompletion requestAwaitingCompletion = responsesToComplete.poll();
                    ResponseListener listener = requestAwaitingCompletion.listener;
                    listener.incomingResponse(resp, requestAwaitingCompletion.request, new ResponseId(0), true);
                } else {
                    processingChunked = true;
                    HttpResponse resp = (HttpResponse) msg;
                    RequestAwaitingCompletion requestAwaitingCompletion = responsesToComplete.peek();
                    ResponseListener listener = requestAwaitingCompletion.listener;
                    listener.incomingResponse(resp, requestAwaitingCompletion.request, new ResponseId(0), false);
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
                ResponseListener listener = responsesToComplete.poll().listener;
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
