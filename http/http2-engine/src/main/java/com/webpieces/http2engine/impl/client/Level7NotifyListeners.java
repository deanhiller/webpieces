package com.webpieces.http2engine.impl.client;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level7NotifyListeners implements EngineResultListener {

	private ClientEngineListener resultListener;

	public Level7NotifyListeners(ClientEngineListener socketListener) {
		this.resultListener = socketListener;
	}

	public void sendPreface(DataWrapper prefaceData) {
		ByteBuffer buffer = ByteBuffer.wrap(prefaceData.createByteArray());
		sendToSocket(buffer);
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return resultListener.sendToSocket(buffer);
	}

	@Override
	public CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		try {
			if(msg instanceof GoAwayFrame) {
				resultListener.sendControlFrameToClient((Http2Frame) msg);
			} else
				throw new UnsupportedOperationException("not done yet. frame="+msg);
			
			future.complete(null);
		} catch(Throwable e) {
			future.completeExceptionally(new RuntimeException(e));
		}
		return future;
	}
	
	public void farEndClosed() {
		this.resultListener.engineClosedByFarEnd();
	}

	@Override
	public CompletableFuture<Void> sendPieceToClient(Stream stream, PartialStream payload) {
		if(payload.getStreamId() % 2 == 1 && !(payload instanceof Http2Push)) {
			Http2ResponseListener listener = stream.getResponseListener();
			return listener.incomingPartialResponse(payload);
		} else {
			PushPromiseListener listener = stream.getPushListener();
			return listener.incomingPushPromise(payload);
		}		
	}

}
