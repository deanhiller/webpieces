package org.webpieces.httpcommon.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;

public interface ResponseSender {

	CompletableFuture<Void> close();

    // When starting a response return a responseid
	// (which could be the streamid) so that we can match sendData calls to the original request
    // This is not used for http/1.1
	CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete);

	CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isComplete);

	void sendTrailer(List<HasHeaderFragment.Header> headerList, ResponseId id, boolean isComplete);

	CompletableFuture<Void> sendException(HttpException e);

	Channel getUnderlyingChannel();

	Protocol getProtocol();
}
