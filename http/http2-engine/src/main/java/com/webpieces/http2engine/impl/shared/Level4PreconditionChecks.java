package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.error.ConnReset2;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ConnectionFailure;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public abstract class Level4PreconditionChecks<T> {

	private final static Logger log = LoggerFactory.getLogger(Level4PreconditionChecks.class);
	
	private Level5CStateMachine stateMachine;
	
	public Level4PreconditionChecks(Level5CStateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	public CompletableFuture<Void> sendDataToApp(DataFrame frame) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}

		return stateMachine.sendDataToApp(frame);
	}
	
	public CompletableFuture<Void> sendTrailersToApp(Http2Trailers frame) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		return stateMachine.sendTrailersToApp(frame);
	}
	
	public CompletableFuture<Void> sendRstToApp(RstStreamFrame frame) {
		return stateMachine.fireRstToClient(frame);
	}
	
	public CompletableFuture<Void> sendRstToServerAndApp(StreamException e) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming reset since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		return stateMachine.sendRstToServerAndApp(e);
	}

	public CompletableFuture<Void> sendGoAwayToApp(ConnReset2 reset) {	
		return stateMachine.sendGoAwayToApp(reset);
	}
	
	public CompletableFuture<Void> sendGoAwayToSvrAndResetAllToApp(ShutdownConnection reset) {
		return stateMachine.sendGoAwayToSvrAndResetAllToApp(reset);
	}

	
	public CompletableFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		if(stateMachine.getClosedReason()  != null) {
			log.info("ignoring incoming window update since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		if(msg.getStreamId() == 0) {
			return stateMachine.connectionWindowUpdate(msg);
		} else {
			return stateMachine.streamWindowUpdate(msg);
		}
	}

	public CompletableFuture<Void> sendDataToSocket(Stream stream, StreamMsg data) {
		ConnectionCancelled closedReason = stateMachine.getClosedReason();
		if(closedReason != null) {
			return createExcepted(data, "sending data", closedReason);
		}
		
		return fireToSocket(stream, data, false);
	}
	
	public CompletableFuture<Void> createExcepted(Http2Msg payload, String extra, ConnectionCancelled closedReason) {
		log.info("returning CompletableFuture.exception since this socket is closed('"+extra+"' frame="+payload+"):"+closedReason.getReasonCode());
		CompletableFuture<Void> future = new CompletableFuture<>();
		ConnectionClosedException exception = new ConnectionClosedException(closedReason, "Connection closed or closing:"+closedReason.getReasonCode());

		if(closedReason instanceof ConnectionFailure) {
			ConnectionFailure fail = (ConnectionFailure) closedReason;
			exception.initCause(fail.getCause());
		}
			
		future.completeExceptionally(exception);

		return future;
	}
	
	public CompletableFuture<Void> sendRstToSocket(Stream stream, RstStreamFrame frame) {
		return stateMachine.fireRstToSocket(stream, frame);
	}
	
	protected CompletableFuture<Void> fireToSocket(Stream stream, StreamMsg frame, boolean keepDelayedState) {
		return stateMachine.fireToSocket(stream, frame, keepDelayedState);
	}
	

	public CompletableFuture<Void> sendPriorityFrameToApp(PriorityFrame frame) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}	
		
		return stateMachine.firePriorityToClient(frame);
	}

	public CompletableFuture<Void> sendUnknownFrame(UnknownFrame msg) {
		return stateMachine.sendUnkownFrame(msg);
	}

}
