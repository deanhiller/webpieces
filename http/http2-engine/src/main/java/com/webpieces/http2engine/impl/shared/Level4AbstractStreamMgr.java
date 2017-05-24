package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level4AbstractStreamMgr<T> {

	private final static Logger log = LoggerFactory.getLogger(Level4AbstractStreamMgr.class);
	protected StreamState streamState;
	protected HeaderSettings remoteSettings;
	private Level6RemoteFlowControl remoteFlowControl;
	private Level6LocalFlowControl localFlowControl;
	protected ConnectionReset closedReason;
	private Level5AbstractStateMachine stateMachine;
	
	public Level4AbstractStreamMgr(
			Level5AbstractStateMachine stateMachine, Level6RemoteFlowControl level5RemoteFlow, 
			Level6LocalFlowControl localFlowControl, 
			HeaderSettings remoteSettings2,
			StreamState streamState) {
		this.stateMachine = stateMachine;
		this.remoteFlowControl = level5RemoteFlow;
		this.localFlowControl = localFlowControl;
		this.remoteSettings = remoteSettings2;
		this.streamState = streamState;
	}

	public abstract CompletableFuture<Void> sendPayloadToApp(PartialStream msg);

	public CompletableFuture<Void> sendRstToApp(RstStreamFrame frame) {
		Stream stream = streamState.getStream(frame, true);
		return sendResetToApp(stream, frame, false);
	}
	
	public CompletableFuture<Void> sendRstToServerAndApp(StreamException e) {
		if(closedReason != null) {
			log.info("ignoring incoming reset since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		RstStreamFrame frame = new RstStreamFrame();
		frame.setKnownErrorCode(e.getReason().getErrorCode());
		frame.setStreamId(e.getStreamId());
		
		boolean streamExist = streamState.isStreamExist(frame);
		if(streamExist) {
			Stream stream = streamState.getStream(frame, true);
			
			return fireRstToSocket(stream, frame)
					.thenCompose( v -> sendResetToApp(stream, frame, false));
		} else {
			//no stream means idle or closed...
			return remoteFlowControl.fireResetToSocket(frame);
		}
	}

	public CompletableFuture<Void> sendGoAwayToApp(ConnectionReset reset) {		
		resetAllClientStreams(reset);
		return CompletableFuture.completedFuture(null);
	}
	
	public CompletableFuture<Void> sendGoAwayToSvrAndResetAllToApp(ConnectionReset reset) {
		closedReason = reset;
		
		remoteFlowControl.goAway(reset.getCause());
		
		resetAllClientStreams(reset);
		
		//since we are closing...mark closed so app can start failing immediately (rather than wait for it to write to nic)
		return CompletableFuture.completedFuture(null);
	}

	private void resetAllClientStreams(ConnectionReset reset) {
		ConcurrentMap<Integer, Stream> streams = streamState.closeEngine();
		for(Stream stream : streams.values()) {
			sendResetToApp(stream, reset, false);
		}		
	}

	protected CompletableFuture<Void> sendResetToApp(Stream stream, RstStreamFrame payload, boolean keepDelayedState) {
		return stateMachine.fireToClient(stream, payload).thenApply(v -> {
			checkForClosedState(stream, payload, keepDelayedState);
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
			Stream stream = streamState.getStream(msg, true);
			if(stream != null)
				return remoteFlowControl.updateStreamWindowSize(stream, msg);
			return CompletableFuture.completedFuture(null);
		}
	}

	public CompletableFuture<Void> sendData(Stream stream, PartialStream data) {
		if(closedReason != null) {
			return createExcepted(data, "sending data");
		}
		
		return fireToSocket(stream, data, false);
	}
	
	public CompletableFuture<Void> createExcepted(Http2Msg payload, String extra) {
		log.info("returning CompletableFuture.exception since this socket is closed('"+extra+"' frame="+payload+"):"+closedReason.getReason());
		CompletableFuture<Void> future = new CompletableFuture<>();

		ConnectionClosedException exception = new ConnectionClosedException("Connection closed or closing:"+closedReason.getReason());
		if(closedReason.getCause() != null)
			exception.initCause(closedReason.getCause());
		future.completeExceptionally(exception);
		return future;
	}
	
	public CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		return fireToSocket(stream, frame, true);
	}
	
	protected CompletableFuture<Void> fireToSocket(Stream stream, PartialStream frame, boolean keepDelayedState) {
		return stateMachine.fireToSocket(stream, frame).thenApply(v -> {
			checkForClosedState(stream, frame, keepDelayedState);
			return null;
		});
	}
	
	/**
	 * Returns if calling this resulted in closing the stream and cleaning up state
	 */
	protected void checkForClosedState(Stream stream, Http2Msg cause, boolean keepDelayedState) {
		//If a stream ends up in closed state, for request type streams, we must release a permit on
		//max concurrent streams
		boolean isClosed = stateMachine.isInClosedState(stream);
		if(!isClosed)
			return; //do nothing
		
		log.info("stream closed="+stream.getStreamId());
		
		if(!keepDelayedState) {
			Stream removedStream = streamState.remove(stream, cause);
			if(removedStream == null)
				return; //someone else closed the stream. they beat us to it so just return
		} else {
			//streamState.addDelayedRemove(stream, afterResetExpireSeconds);
			throw new UnsupportedOperationException("not supported");
		}
		
	}
	

	public CompletableFuture<Void> sendPriorityFrame(PriorityFrame frame) {
		if(closedReason != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		Stream stream;
		try {
			stream = streamState.getStream(frame, true);
		} catch(ConnectionException e) {
			//per spec, priority frames can be received on closed stream but ignore it
			return CompletableFuture.completedFuture(null);
		}			
		
		return stateMachine.firePriorityToClient(stream, frame)
						.thenApply(s -> null);

	}

}
