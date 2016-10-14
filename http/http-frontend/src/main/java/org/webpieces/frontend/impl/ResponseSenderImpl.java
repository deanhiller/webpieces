package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.channels.Channel;

public class ResponseSenderImpl implements ResponseSender {

	private Channel channel;
	private HttpParser parser;

	public ResponseSenderImpl(Channel channel, HttpParser parser) {
		this.channel = channel;
		this.parser = parser;
	}

	// HTTP/1.1 doesn't need request ids so we're just going to return zero right now
	private ResponseId getNextResponseId() {
		return new ResponseId(0);
	}

	@Override
	public CompletableFuture<Void> close() {
		return channel.close().thenAccept(c -> {});
	}
	
	@Override
	public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, boolean isComplete) {
		ByteBuffer data = parser.marshalToByteBuffer(response);
		ResponseId id = getNextResponseId();
		return channel.write(data).thenApply(c -> id);
	}

	@Override
	public CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isLastData) {
		// Create a chunk from the data, then send.
		HttpChunk chunk;
		if(isLastData) {
			chunk = new HttpLastChunk();
		} else
		{
			chunk = new HttpChunk();
		}
		chunk.setBody(data);
		return channel.write(parser.marshalToByteBuffer(chunk)).thenAccept(c -> {});
	}

	@Override
	public CompletableFuture<Void> sendException(HttpException e) {

		HttpResponseStatus respStatus = new HttpResponseStatus();
		respStatus.setKnownStatus(e.getStatusCode());
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(respStatus);

		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine );

		response.addHeader(new Header("Failure", e.getMessage()));
		ByteBuffer data = parser.marshalToByteBuffer(response);

		return channel.write(data).thenAccept(c -> {});
	}

	@Override
	public Channel getUnderlyingChannel() {
		return channel;
	}

	@Override
	public String toString() {
		return "ResponseSender[" + channel + "]";
	}

	
}
