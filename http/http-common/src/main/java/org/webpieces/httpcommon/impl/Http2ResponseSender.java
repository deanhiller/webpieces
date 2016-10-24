package org.webpieces.httpcommon.impl;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.*;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class Http2ResponseSender implements ResponseSender {
    // TODO: maybe we should just make the Http2ServerEngine implement ResponseSender?
    private Http2Engine http2Engine;

    public Http2ResponseSender(Http2Engine http2Engine) {
        this.http2Engine = http2Engine;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.HTTP2;
    }

    @Override
    public CompletableFuture<Void> close() {
        // TODO: Do some stuff with the engine to close
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete) {
        return http2Engine.sendResponse(response, request, requestId, isComplete);
    }

    @Override
    public CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isComplete) {
        return http2Engine.sendData(data, id, isComplete);
    }

    @Override
    public void sendTrailer(List<HasHeaderFragment.Header> headerList, ResponseId id, boolean isComplete) {
        // TODO: implement this to support chunked encoding last chunk headers in http2
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> sendException(HttpException e) {
        // TODO: ?? what do we do here
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Channel getUnderlyingChannel() {
        return http2Engine.getUnderlyingChannel();
    }
}


