package org.webpieces.httpcommon.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

public interface ResponseSender {

	CompletableFuture<Void> close();

	ResponseId getNextResponseId();

    // When starting a response we set the responseid so that we can match sendData calls to the original request
    // This is not used for complete responses or http/1.1
	CompletableFuture<Void> sendResponse(HttpResponse response, HttpRequest request, ResponseId id, boolean isComplete);

	CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isLastData);

	Channel getUnderlyingChannel();
}
