package org.webpieces.httpclient.impl2;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.dto.Http2Headers;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2UnknownFrame;
import com.webpieces.http2parser.api.highlevel.Http2FullHeaders;
import com.webpieces.http2parser.api.highlevel.Http2Payload;
import com.webpieces.http2parser.api.highlevel.Http2TrailingHeaders;
import com.webpieces.http2parser.api.highlevel.ResultListener;

public class Layer5Outgoing implements ResultListener {
	
	private TCPChannel channel;
	
	private Map<Integer, Http2ResponseListener> streamIdToListener = new HashMap<>();
	private Http2ServerListener clientListener;

	public Layer5Outgoing(TCPChannel channel) {
		this.channel = channel;
	}

	@Override
	public void incomingPayload(Http2Payload frame) {
		if(frame.getStreamId() == 0)
			throw new IllegalArgumentException("control frames should not come up this far. type="+frame.getClass());
		else if(frame.getStreamId() % 2 == 0) {
			throw new UnsupportedOperationException("Server push frames not supported yet");
		}
		
		incomingClientResponse(frame);
	}

	private void incomingClientResponse(Http2Payload frame) {
		Http2ResponseListener listener = streamIdToListener.get(frame.getStreamId());
		if(listener == null)
			throw new IllegalStateException("missing listener for stream id="+frame.getStreamId());
		else if(frame instanceof Http2FullHeaders) {
			Http2FullHeaders head = (Http2FullHeaders) frame;
			Http2Headers headers = new Http2Headers(head.getHeaderList());
			listener.incomingResponse(headers);
		} else if(frame instanceof Http2Data) {
			Http2Data data = (Http2Data) frame;
			listener.incomingData(data.getData());
		} else if(frame instanceof Http2TrailingHeaders) {
			Http2TrailingHeaders head = (Http2TrailingHeaders) frame;
			Http2Headers headers = new Http2Headers(head.getHeaderList());
			listener.incomingResponse(headers);			
		} else if(frame instanceof Http2UnknownFrame) {
			listener.incomingUnknownFrame((Http2UnknownFrame) frame);
		} else
			throw new IllegalArgumentException("client was not expecting frame type="+frame.getClass());
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer data) {
		return channel.write(data)
						.thenApply(c -> null);
	}

	public void setClientListener(Http2ServerListener listener) {
		this.clientListener = listener;
	}

	public Http2ServerListener getClientListener() {
		return clientListener;
	}

	public void sendPreface(ByteBuffer buf) {
		channel.write(buf);
	}

	public CompletableFuture<Channel> connect(InetSocketAddress addr, Layer1Incoming incoming) {
		return channel.connect(addr, incoming);
	}

	public CompletableFuture<Channel> close() {
		return channel.close();
	}

	public void addResponseListener(int streamId, Http2ResponseListener listener) {
		streamIdToListener.put(streamId, listener);
	}

}
