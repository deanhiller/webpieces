package org.webpieces.httpclient11.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.nio.api.channels.TCPChannel;

public class HttpChunkWriterImpl implements HttpDataWriter {

	private TCPChannel channel;
	private HttpParser parser;
	private MarshalState state;

	public HttpChunkWriterImpl(TCPChannel channel, HttpParser parser, MarshalState state) {
		this.channel = channel;
		this.parser = parser;
		this.state = state;
	}

	@Override
	public CompletableFuture<Void> send(HttpData chunk) {
		ByteBuffer buffer = parser.marshalToByteBuffer(state, chunk);
		return channel.write(buffer);
	}

}
