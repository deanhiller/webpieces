package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.PushPromiseListener;
import org.webpieces.http2client.api.dto.Http2Data;
import org.webpieces.http2client.api.dto.Http2Headers;
import org.webpieces.http2client.api.dto.Http2Push;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.Http2FullHeaders;
import com.webpieces.http2engine.api.Http2FullPushPromise;
import com.webpieces.http2engine.api.Http2Payload;
import com.webpieces.http2engine.api.ResultListener;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class Layer5Outgoing implements ResultListener {
	
	private static final Logger log = LoggerFactory.getLogger(Layer5Outgoing.class);
	private TCPChannel channel;
	
	private Map<Integer, Http2ResponseListener> streamIdToListener = new HashMap<>();
	private Map<Integer, PushPromiseListener> streamIdToPushListener = new HashMap<>();
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
		else if(frame.getStreamId() % 2 == 1) {
			incomingClientResponse(frame);
		} else if(frame.getStreamId() % 2 == 0) {
			incomingPush(frame);
		}
	}

	private void incomingPush(Http2Payload frame) {
		if(frame instanceof Http2FullPushPromise) {
			Http2FullPushPromise promise = (Http2FullPushPromise) frame;
			int newStreamId = promise.getStreamId();
			Http2ResponseListener listener = streamIdToListener.get(promise.getOriginalStreamId());
			PushPromiseListener pushListener = listener.newIncomingPush(newStreamId);
			
			Http2Push push = new Http2Push(promise.getHeaderList());
			push.setStreamId(newStreamId);
			
			streamIdToPushListener.put(newStreamId, pushListener);
			pushListener.incomingPushPromise(push);
			return;
		}
		
		int streamId = frame.getStreamId();
		PushPromiseListener listener = streamIdToPushListener.get(streamId);
		
		if(frame.isEndStream()) { //clean up memory if last data for push_promise
			streamIdToPushListener.remove(streamId);
		}
		
		if(listener == null)
			throw new IllegalStateException("missing listener for stream id="+frame.getStreamId());
		else if(frame instanceof Http2FullHeaders) {
			Http2FullHeaders head = (Http2FullHeaders) frame;
			Http2Headers headers = new Http2Headers(head.getHeaderList());
			headers.setStreamId(head.getStreamId());
			headers.setLastPartOfResponse(head.isEndStream());
			listener.incomingPushPromise(headers);
		} else if(frame instanceof DataFrame) {
			DataFrame data = (DataFrame) frame;
			Http2Data http2Data = new Http2Data();
			http2Data.setStreamId(data.getStreamId());
			http2Data.setLastPartOfResponse(data.isEndStream());
			http2Data.setPayload(data.getData());
			listener.incomingPushPromise(http2Data);
		} else if(frame instanceof UnknownFrame) {
			//drop it, though we could feed to client if they are testing out some SIP protocol or something
		} else
			throw new IllegalArgumentException("client was not expecting frame type="+frame.getClass());
	}

	private void incomingClientResponse(Http2Payload frame) {
		int streamId = frame.getStreamId();
		Http2ResponseListener listener = streamIdToListener.get(streamId);
		
		if(frame.isEndStream()) { //clean up memory if last response
			streamIdToListener.remove(streamId);
		}
		
		if(listener == null)
			throw new IllegalStateException("missing listener for stream id="+streamId);
		else if(frame instanceof Http2FullHeaders) {
			Http2FullHeaders head = (Http2FullHeaders) frame;
			Http2Headers headers = new Http2Headers(head.getHeaderList());
			headers.setStreamId(head.getStreamId());
			headers.setLastPartOfResponse(head.isEndStream());
			listener.incomingPartialResponse(headers);
		} else if(frame instanceof DataFrame) {
			DataFrame data = (DataFrame) frame;
			Http2Data http2Data = new Http2Data();
			http2Data.setStreamId(data.getStreamId());
			http2Data.setLastPartOfResponse(data.isEndStream());
			http2Data.setPayload(data.getData());
			listener.incomingPartialResponse(http2Data);
		} else if(frame instanceof UnknownFrame) {
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
