package org.webpieces.httpclient.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.HttpChunkWriter;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.nio.api.channels.TCPChannel;

public class HttpChunkWriterImpl implements HttpChunkWriter {

	private TCPChannel channel;
	private HttpParser parser;

	public HttpChunkWriterImpl(TCPChannel channel, HttpParser parser) {
		this.channel = channel;
		this.parser = parser;
	}

	@Override
	public CompletableFuture<Void> send(HttpChunk chunk) {
		ByteBuffer buffer = parser.marshalToByteBuffer(chunk);
		return channel.write(buffer).thenApply(c -> null);
	}

}
