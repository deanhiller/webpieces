package org.webpieces.httpcommon.impl;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.Http2Engine;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

import java.util.concurrent.CompletableFuture;

public class Http2ResponseSender implements ResponseSender {
    // TODO: maybe we should just make the Http2ServerEngine implement ResponseSender?
    private Http2Engine http2Engine;

    public Http2ResponseSender(Http2Engine http2Engine) {
        this.http2Engine = http2Engine;
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
    public CompletableFuture<Void> sendException(HttpException e) {
        // TODO: ?? what do we do here
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Channel getUnderlyingChannel() {
        return http2Engine.getUnderlyingChannel();
    }
}


