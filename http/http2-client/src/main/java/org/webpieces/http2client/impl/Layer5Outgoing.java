package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.Http2Data;
import org.webpieces.http2client.api.dto.Http2Headers;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.Http2FullHeaders;
import com.webpieces.http2engine.api.Http2Payload;
import com.webpieces.http2engine.api.ResultListener;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2UnknownFrame;

public class Layer5Outgoing implements ResultListener {
	
	private static final Logger log = LoggerFactory.getLogger(Layer5Outgoing.class);
	private TCPChannel channel;
	
	private Map<Integer, Http2ResponseListener> streamIdToListener = new HashMap<>();
	private Http2ServerListener clientListener;
	private Http2Socket socket;

	public Layer5Outgoing(TCPChannel channel, Http2Socket socket) {
		this.channel = channel;
		this.socket = socket;
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
			headers.setLastPartOfResponse(head.isEndStream());
			listener.incomingPartialResponse(headers);
		} else if(frame instanceof DataFrame) {
			DataFrame data = (DataFrame) frame;
			Http2Data http2Data = new Http2Data();
			http2Data.setLastPartOfResponse(data.isEndStream());
			http2Data.setPayload(data.getData());
			listener.incomingPartialResponse(http2Data);
		} else if(frame instanceof Http2UnknownFrame) {
			//drop it, though we could feed to client if they are testing out some SIP protocol or something
		} else
			throw new IllegalArgumentException("client was not expecting frame type="+frame.getClass());
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer data) {
		log.info("writing out data to socket size="+data.remaining());
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

	@Override
	public void incomingControlFrame(Http2Frame lowLevelFrame) {
		clientListener.incomingControlFrame(lowLevelFrame);
	}

	@Override
	public void engineClosed() {
		clientListener.farEndClosed(socket);
	}

}
