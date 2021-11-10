package com.webpieces.http2engine.impl.shared;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.error.StreamException;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.PriorityFrame;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.UnknownFrame;
import com.webpieces.http2.api.dto.lowlevel.WindowUpdateFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.api.error.ConnReset2;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ConnectionClosedException;
import com.webpieces.http2engine.api.error.ConnectionFailure;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.Stream;

public abstract class Level4PreconditionChecks<T> {

	private final static Logger log = LoggerFactory.getLogger(Level4PreconditionChecks.class);
	
	private Level5CStateMachine stateMachine;
	
	public Level4PreconditionChecks(Level5CStateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	public XFuture<Void> sendDataToApp(DataFrame frame) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return XFuture.completedFuture(null);
		}

		return stateMachine.sendDataToApp(frame);
	}
	
	public XFuture<Void> sendTrailersToApp(Http2Trailers frame) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return XFuture.completedFuture(null);
		}
		
		return stateMachine.sendTrailersToApp(frame);
	}
	
	public XFuture<Void> sendRstToApp(RstStreamFrame frame) {
		return stateMachine.fireRstToClient(frame);
	}
	
	public XFuture<Void> sendRstToServerAndApp(StreamException e) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming reset since socket is shutting down");
			return XFuture.completedFuture(null);
		}
		return stateMachine.sendRstToServerAndApp(e);
	}

	public XFuture<Void> sendGoAwayToApp(ConnReset2 reset) {
		return stateMachine.sendGoAwayToApp(reset);
	}
	
	public XFuture<Void> sendGoAwayToSvrAndResetAllToApp(ShutdownConnection reset) {
		return stateMachine.sendGoAwayToSvrAndResetAllToApp(reset);
	}

	
	public XFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		if(stateMachine.getClosedReason()  != null) {
			log.info("ignoring incoming window update since socket is shutting down");
			return XFuture.completedFuture(null);
		}
		
		if(msg.getStreamId() == 0) {
			return stateMachine.connectionWindowUpdate(msg);
		} else {
			return stateMachine.streamWindowUpdate(msg);
		}
	}

	public XFuture<Void> sendDataToSocket(Stream stream, StreamMsg data) {
		ConnectionCancelled closedReason = stateMachine.getClosedReason();
		if(closedReason != null) {
			return createExcepted(data, "sending data", closedReason);
		}
		
		return fireToSocket(stream, data, false);
	}
	
	public XFuture<Void> createExcepted(Http2Msg payload, String extra, ConnectionCancelled closedReason) {
		log.info("returning XFuture.exception since this socket is closed('"+extra+"' frame="+payload+"):"+closedReason.getReasonCode());
		XFuture<Void> future = new XFuture<>();
		ConnectionClosedException exception = new ConnectionClosedException(closedReason, "Connection closed or closing:"+closedReason.getReasonCode());

		if(closedReason instanceof ConnectionFailure) {
			ConnectionFailure fail = (ConnectionFailure) closedReason;
			exception.initCause(fail.getCause());
		}
			
		future.completeExceptionally(exception);

		return future;
	}
	
	public XFuture<Void> sendRstToSocket(Stream stream, RstStreamFrame frame) {
		return stateMachine.fireRstToSocket(stream, frame);
	}
	
	protected XFuture<Void> fireToSocket(Stream stream, StreamMsg frame, boolean keepDelayedState) {
		return stateMachine.fireToSocket(stream, frame, keepDelayedState);
	}
	

	public XFuture<Void> sendPriorityFrameToApp(PriorityFrame frame) {
		if(stateMachine.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return XFuture.completedFuture(null);
		}	
		
		return stateMachine.firePriorityToClient(frame);
	}

	public XFuture<Void> sendUnknownFrame(UnknownFrame msg) {
		return stateMachine.sendUnkownFrame(msg);
	}

}
