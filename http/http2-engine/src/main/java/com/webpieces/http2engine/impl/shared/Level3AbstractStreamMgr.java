package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level3AbstractStreamMgr {

	protected StreamState streamState;
	protected HeaderSettings remoteSettings;
	private Level5RemoteFlowControl remoteFlowControl;

	public Level3AbstractStreamMgr(Level5RemoteFlowControl level5FlowControl2) {
		this.remoteFlowControl = level5FlowControl2;
	}

	public abstract CompletableFuture<Void> sendPayloadToClient(PartialStream msg);

	public CompletableFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		if(msg.getStreamId() == 0) {
			return remoteFlowControl.updateConnectionWindowSize(msg);
		} else {
			Stream stream = streamState.get(msg);
			return remoteFlowControl.updateStreamWindowSize(stream, msg);
		}
	}

	public void setMaxConcurrentStreams(long value) {
		remoteSettings.setMaxConcurrentStreams(value);
	}
}
