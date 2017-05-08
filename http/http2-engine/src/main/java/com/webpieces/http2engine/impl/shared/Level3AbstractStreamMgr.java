package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.StreamException;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level3AbstractStreamMgr {

	private final static Logger log = LoggerFactory.getLogger(Level3AbstractStreamMgr.class);
	protected StreamState streamState;
	protected HeaderSettings remoteSettings;
	private Level5RemoteFlowControl remoteFlowControl;
	private Level5LocalFlowControl localFlowControl;
	protected ConnectionException closedReason;
	
	public Level3AbstractStreamMgr(
			Level5RemoteFlowControl level5RemoteFlow, 
			Level5LocalFlowControl localFlowControl, 
			HeaderSettings remoteSettings2) {
		this.remoteFlowControl = level5RemoteFlow;
		this.localFlowControl = localFlowControl;
		this.remoteSettings = remoteSettings2;
	}

	public abstract CompletableFuture<Void> sendPayloadToClient(PartialStream msg);
	protected abstract CompletableFuture<Void> fireToSocket(Stream stream, RstStreamFrame frame);

	public CompletableFuture<Void> sendRstToServerAndClient(StreamException e) {
		if(closedReason != null) {
			log.info("ignoring incoming reset since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		RstStreamFrame frame = new RstStreamFrame();
		frame.setKnownErrorCode(e.getReason().getErrorCode());
		frame.setStreamId(e.getStreamId());
		
		Stream stream = streamState.getStream(frame);
		return fireToSocket(stream, frame)
			.thenCompose(t -> localFlowControl.fireToClient(stream, frame));
	}
	
	public CompletableFuture<Void> sendClientResetsAndSvrGoAway(ConnectionException e) {
		closedReason = e;
		ConcurrentMap<Integer, Stream> streams = streamState.closeEngine(e);
		for(Stream stream : streams.values()) {
			Http2ResponseListener responseListener = stream.getResponseListener();
			if(responseListener != null)
				fireReset(e, (c) -> responseListener.incomingPartialResponse(c));
			PushPromiseListener pushListener = stream.getPushListener();
			if(pushListener != null)
				fireReset(e, (c) -> pushListener.incomingPushPromise(c));
		}
		
		return remoteFlowControl.goAway(e);
	}

	private void fireReset(ConnectionException e, Function<ConnectionReset, CompletableFuture<Void>> clientFunction) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		try {
			future = clientFunction.apply(new ConnectionReset(e));
		} catch(Throwable t) {
			future.completeExceptionally(t);
		}
		
		future.exceptionally((t) -> {
			log.error("when trying inform client of connection error, client had an exception", t);
			return null;
		});
	}
	
	public CompletableFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		if(closedReason != null) {
			log.info("ignoring incoming window update since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		} else if(msg.getStreamId() == 0) {
			return remoteFlowControl.updateConnectionWindowSize(msg);
		} else {
			Stream stream = streamState.getStream(msg);
			return remoteFlowControl.updateStreamWindowSize(stream, msg);
		}
	}

	public void setMaxConcurrentStreams(long value) {
		remoteSettings.setMaxConcurrentStreams(value);
		
		modifyMaxConcurrentStreams(value);
	}

	protected abstract void modifyMaxConcurrentStreams(long value);

}
