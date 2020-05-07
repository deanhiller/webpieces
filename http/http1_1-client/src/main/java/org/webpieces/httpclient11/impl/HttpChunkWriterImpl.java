package org.webpieces.httpclient11.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.dto.HttpData;

public class HttpChunkWriterImpl implements HttpDataWriter {

	private ChannelProxy channel;
	private HttpParser parser;
	private MarshalState state;
	private boolean isConnectMsg;

	public HttpChunkWriterImpl(ChannelProxy channel, HttpParser parser, MarshalState state, boolean isConnectMsg) {
		this.channel = channel;
		this.parser = parser;
		this.state = state;
		this.isConnectMsg = isConnectMsg;
	}

	@Override
	public CompletableFuture<Void> send(HttpData chunk) {
		if(isConnectMsg) {
			//special case where we don't go through parser as SSL is being passed through now
			DataWrapper body = chunk.getBody();
			byte[] createByteArray = body.createByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(createByteArray);
			return channel.write(buffer);
		}
		
		ByteBuffer buffer = parser.marshalToByteBuffer(state, chunk);
		return channel.write(buffer);
	}

}
