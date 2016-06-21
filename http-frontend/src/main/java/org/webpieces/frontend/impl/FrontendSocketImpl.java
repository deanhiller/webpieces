package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.nio.api.channels.Channel;

public class FrontendSocketImpl implements FrontendSocket {

	private Channel channel;
	private HttpParser parser;

	public FrontendSocketImpl(Channel channel, HttpParser parser) {
		this.channel = channel;
		this.parser = parser;
	}

	@Override
	public CompletableFuture<FrontendSocket> close() {
		return channel.close().thenApply(c -> this);
	}
	
	@Override
	public CompletableFuture<FrontendSocket> write(HttpPayload payload) {
		if(payload.getMessageType() == HttpMessageType.REQUEST)
			throw new IllegalArgumentException("can't send request back to client.  must send response or chunks");
		
		ByteBuffer data = parser.marshalToByteBuffer(payload);
		
		return channel.write(data).thenApply(c -> this);
	}

	@Override
	public Channel getUnderlyingChannel() {
		return channel;
	}

	@Override
	public String toString() {
		return "FrontendSocket[channel=" + channel + "]";
	}

	
}
