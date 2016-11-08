package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpcommon.api.Protocol;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.channels.Channel;

class Http11ResponseSender implements ResponseSender {

	private Channel channel;
	private HttpParser parser;

	public Http11ResponseSender(Channel channel, HttpParser parser) {
		this.channel = channel;
		this.parser = parser;
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.HTTP11;
	}

	@Override
	public CompletableFuture<Void> close() {
		return channel.close().thenAccept(c -> {});
	}

	private CompletableFuture<Void> actuallySendResponse(HttpResponse response) {
		ByteBuffer data = parser.marshalToByteBuffer(response);
		return channel.write(data).thenAccept(c -> {});

	}
	@Override
	public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete) {
		// HTTP/1.1 doesn't need responseids
		ResponseId id = new ResponseId(0);

		if(isComplete) {
			return actuallySendResponse(response)
					.thenApply(v -> id);
		} else {
			HttpResponse responseNoBody = Responses.copyResponseExceptBody(response);
			DataWrapper body = response.getBodyNonNull();
			// If we're not already chunked, make it chunked.
			Header transferEncodingHeader = responseNoBody.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING);
			if(transferEncodingHeader == null || !transferEncodingHeader.getValue().equalsIgnoreCase("chunked")) {
				responseNoBody.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
			}
			return actuallySendResponse(responseNoBody)
					.thenAccept(v -> {
						if(body.getReadableSize() != 0)
							sendData(body, id, false);
					})
					.thenApply(v -> id);
		}
	}

	@Override
	public CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isComplete) {
		// Create a chunk from the data, then send.
		HttpChunk chunk;
		if(isComplete) {
			chunk = new HttpLastChunk();
		} else
		{
			chunk = new HttpChunk();
		}
		chunk.setBody(data);
		return channel.write(parser.marshalToByteBuffer(chunk)).thenAccept(c -> {});
	}

	@Override
	public void sendTrailer(List<HasHeaderFragment.Header> headerList, ResponseId id, boolean isComplete) {
		if(!isComplete) {
			throw new RuntimeException("can't sendTrailer that isnot completeing the response");
		}
		HttpLastChunk lastChunk = new HttpLastChunk();
		for(HasHeaderFragment.Header header: headerList) {
			lastChunk.addHeader(new Header(header.header, header.value));
		}
		channel.write(parser.marshalToByteBuffer(lastChunk));
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
