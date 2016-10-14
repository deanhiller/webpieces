package org.webpieces.frontend.impl;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.Http2Engine;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

import java.util.concurrent.CompletableFuture;

public class Http2ResponseSender implements ResponseSender {
    private Http2Engine http2Engine;

    @Override
    public CompletableFuture<Void> close() {
        return null;
    }

    @Override
    public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, boolean isComplete) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isLastData) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendException(HttpException e) {
        return null;
    }

    @Override
    public Channel getUnderlyingChannel() {
        return null;
    }
}


