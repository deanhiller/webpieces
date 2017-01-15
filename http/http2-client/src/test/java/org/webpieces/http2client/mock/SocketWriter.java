package org.webpieces.http2client.mock;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class SocketWriter {

	private HpackParser parser;
	private MarshalState marshalState;
	private DataListener listener;
	private TCPChannel channel;

	public SocketWriter(TCPChannel channel, HpackParser parser, MarshalState marshalState, DataListener listener) {
		this.channel = channel;
		this.parser = parser;
		this.marshalState = marshalState;
		this.listener = listener;
	}

	public void write(Http2Msg msg) throws InterruptedException, ExecutionException {
		DataWrapper data = parser.marshal(marshalState, msg);
		byte[] bytes = data.createByteArray();
		if(bytes.length == 0)
			throw new IllegalArgumentException("how do you marshal to 0 bytes...WTF");
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(channel, buf);
	}
}
