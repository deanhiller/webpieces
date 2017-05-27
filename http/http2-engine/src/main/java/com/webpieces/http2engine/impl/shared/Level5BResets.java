package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.error.ConnReset2;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ShutdownStream;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.util.locking.PermitQueue;

public abstract class Level5BResets extends Level5AStates {
	private final static Logger log = LoggerFactory.getLogger(Level5BResets.class);

	private Level6LocalFlowControl localFlowControl;
	protected Level6RemoteFlowControl remoteFlowControl;
	protected ConnectionCancelled closedReason;

	public Level5BResets(String id, StreamState streamState, Level6LocalFlowControl localFlowControl, Level6RemoteFlowControl remoteFlowControl, PermitQueue maxConcurrentQueue) {
		super(id, streamState, maxConcurrentQueue);
		this.localFlowControl = localFlowControl;
		this.remoteFlowControl = remoteFlowControl;
	}

	public ConnectionCancelled getClosedReason() {
		return closedReason;
	}
	
	public CompletableFuture<Void> sendGoAwayToApp(ConnReset2 reset) {
		resetAllClientStreams(reset);
		return CompletableFuture.completedFuture(null);
	}
	
	public CompletableFuture<Void> sendRstToServerAndApp(StreamException e) {
		RstStreamFrame frame = new RstStreamFrame();
		frame.setKnownErrorCode(e.getReason().getErrorCode());
		frame.setStreamId(e.getStreamId());
		
		boolean streamExist = streamState.isStreamExist(frame);
		if(streamExist) {
			Stream stream = streamState.getStream(frame, true);
			
			return fireRstToSocket(stream, frame)
					.thenCompose( v -> {
							CompletableFuture<Void> future = fireRstToClient(stream, frame);
							return future;
						});
		} else {
			//no stream means idle or closed...
			return remoteFlowControl.fireResetToSocket(frame);
		}
	}
	
	public CompletableFuture<Void> sendGoAwayToSvrAndResetAllToApp(ShutdownConnection reset) {
		closedReason = reset;
		
		remoteFlowControl.goAway(reset);
		
		resetAllClientStreams(reset);
		
		//since we are closing...mark closed so app can start failing immediately (rather than wait for it to write to nic)
		return CompletableFuture.completedFuture(null);
	}

	private void resetAllClientStreams(ConnectionCancelled reset) {
		ConcurrentMap<Integer, Stream> streams = streamState.closeEngine();
		for(Stream stream : streams.values()) {
			ShutdownStream str = new ShutdownStream(stream.getStreamId(), reset);
			fireRstToClient(stream, str);
		}		
	}
	
	public CompletableFuture<Void> fireRstToClient(RstStreamFrame frame) {
		Stream stream = streamState.getStream(frame, true);
		return fireRstToClient(stream, frame);
	}
	
	public CompletableFuture<Void> fireRstToClient(Stream stream, CancelReason frame) { 
		return fireRecvToSM(stream, frame)
				.thenCompose(v-> {
					return localFlowControl.fireRstToClient(stream, frame);
				});
	}
	
	public CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		return fireSendToSM(stream, frame, true)
				.thenCompose(v -> {
					return remoteFlowControl.sendPayloadToSocket(stream, frame);
				});
	}
	
}
