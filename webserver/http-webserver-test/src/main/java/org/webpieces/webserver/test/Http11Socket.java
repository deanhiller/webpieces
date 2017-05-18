package org.webpieces.webserver.test;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.nio.api.handlers.DataListener;

public class Http11Socket {

	//This dataListener is the production listener that listens to the socket...
	private DataListener dataListener;
	//This is where the response is written to
	private MockTcpChannel channel;
	private HttpParser parser;

	public Http11Socket(DataListener dataListener, MockTcpChannel channel, HttpParser parser) {
		this.dataListener = dataListener;
		this.channel = channel;
		this.parser = parser;
	}
	
	public void send(HttpPayload payload) {
		ByteBuffer buf = parser.marshalToByteBuffer(payload);
		dataListener.incomingData(channel, buf);
	}
	
	public void close() {
		dataListener.farEndClosed(channel);
	}

	public List<FullResponse> getResponses() {
		return channel.getResponses();
	}

	public void sendBytes(DataWrapper dataWrapper) {
		byte[] bytes = dataWrapper.createByteArray();
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		dataListener.incomingData(channel, wrap);
	}

	public void clear() {
		channel.clear();
	}

}
