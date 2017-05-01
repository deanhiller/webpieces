package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.StreamException;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level3AbstractStreamMgr {

	protected StreamState streamState;
	protected HeaderSettings remoteSettings;
	private Level5RemoteFlowControl remoteFlowControl;
	private Level5LocalFlowControl localFlowControl;
	
	private AtomicReference<ConnectionException> engineClosedReason;

	public Level3AbstractStreamMgr(Level5RemoteFlowControl level5RemoteFlow, Level5LocalFlowControl localFlowControl, HeaderSettings remoteSettings2) {
		this.remoteFlowControl = level5RemoteFlow;
		this.localFlowControl = localFlowControl;
		this.remoteSettings = remoteSettings2;
	}

	public abstract CompletableFuture<Void> sendPayloadToClient(PartialStream msg);
	protected abstract CompletableFuture<Void> fireToSocket(Stream stream, RstStreamFrame frame);

	public CompletableFuture<Void> sendRstToServerAndClient(StreamException e) {		
		RstStreamFrame frame = new RstStreamFrame();
		frame.setKnownErrorCode(e.getReason().getErrorCode());
		frame.setStreamId(e.getStreamId());
		
		Stream stream = streamState.get(frame);
		return fireToSocket(stream, frame)
			.thenCompose(t -> localFlowControl.fireToClient(stream, frame));
	}
	
	public CompletableFuture<Void> sendClientResetsAndSvrGoAway(ConnectionException e) {
		engineClosedReason.set(e);
		return null;
	}
	
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
		
		modifyMaxConcurrentStreams(value);
	}

	protected abstract void modifyMaxConcurrentStreams(long value);

}
