package com.webpieces.http2engine.impl.shared;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ConcurrentMap;

import org.webpieces.util.locking.PermitQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.error.StreamException;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2engine.api.error.ConnReset2;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.api.error.ShutdownStream;
import com.webpieces.http2engine.impl.shared.data.Stream;

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
	
	public XFuture<Void> sendGoAwayToApp(ConnReset2 reset) {
		resetAllClientStreams(reset);
		return XFuture.completedFuture(null);
	}
	
	public XFuture<Void> sendRstToServerAndApp(StreamException e) {
		RstStreamFrame frame = new RstStreamFrame();
		frame.setKnownErrorCode(e.getReason().getErrorCode());
		frame.setStreamId(e.getStreamId());
		
		boolean streamExist = streamState.isStreamExist(frame);
		if(streamExist) {
			Stream stream = streamState.getStream(frame, true);
			
			return fireRstToSocket(stream, frame)
					.thenCompose( v -> {
							XFuture<Void> future = fireRstToClient(stream, frame);
							return future;
						});
		} else {
			//no stream means idle or closed...
			return remoteFlowControl.fireResetToSocket(frame);
		}
	}
	
	public XFuture<Void> sendGoAwayToSvrAndResetAllToApp(ShutdownConnection reset) {
		closedReason = reset;
		
		remoteFlowControl.goAway(reset);
		
		resetAllClientStreams(reset);
		
		//since we are closing...mark closed so app can start failing immediately (rather than wait for it to write to nic)
		return XFuture.completedFuture(null);
	}

	private void resetAllClientStreams(ConnectionCancelled reset) {
		ConcurrentMap<Integer, Stream> streams = streamState.closeEngine();
		for(Stream stream : streams.values()) {
			ShutdownStream str = new ShutdownStream(stream.getStreamId(), reset);
			fireRstToClient(stream, str);
		}		
	}
	
	public XFuture<Void> fireRstToClient(RstStreamFrame frame) {
		Stream stream = streamState.getStream(frame, true);
		return fireRstToClient(stream, frame);
	}
	
	public XFuture<Void> fireRstToClient(Stream stream, CancelReason frame) {
		return fireRecvToSM(stream, frame)
				.thenCompose(v-> {
					return localFlowControl.fireRstToClient(stream, frame);
				});
	}
	
	public XFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		return fireSendToSM(stream, frame, true)
				.thenCompose(v -> {
					return remoteFlowControl.sendPayloadToSocket(stream, frame);
				});
	}
	
}
