package com.webpieces.http2engine.impl.client;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

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

	public void sendControlFrameToClient(Http2Msg msg) {
		if(msg instanceof GoAwayFrame) {
			resultListener.sendControlFrameToClient((Http2Frame) msg);
		} else
			throw new UnsupportedOperationException("not done yet. frame="+msg);
	}
	
	public void farEndClosed() {
		this.resultListener.engineClosedByFarEnd();
	}

}
