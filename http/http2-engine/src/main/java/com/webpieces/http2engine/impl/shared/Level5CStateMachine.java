package com.webpieces.http2engine.impl.shared;

import org.webpieces.util.futures.XFuture;

import org.webpieces.util.locking.PermitQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.PriorityFrame;
import com.webpieces.http2.api.dto.lowlevel.UnknownFrame;
import com.webpieces.http2.api.dto.lowlevel.WindowUpdateFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.impl.shared.data.Stream;

public abstract class Level5CStateMachine extends Level5BResets {

	private static final Logger log = LoggerFactory.getLogger(Level5CStateMachine.class);

	protected Level6LocalFlowControl localFlowControl;

	public Level5CStateMachine(
			String id, 
			StreamState streamState, 
			Level6RemoteFlowControl remoteFlowControl, 
			Level6LocalFlowControl localFlowControl, 
			PermitQueue maxConcurrentQueue
	) {
		super(id, streamState, localFlowControl, remoteFlowControl, maxConcurrentQueue);
		this.localFlowControl = localFlowControl;
	}

	public XFuture<Void> fireToSocket(Stream stream, Http2Msg payload) {
		return fireSendToSM(stream, payload, false)
				.thenCompose(v -> {
					return remoteFlowControl.sendPayloadToSocket(stream, payload);
				});
	}
	
	public XFuture<Void> fireToSocket(Stream stream, StreamMsg payload, boolean keepDelayedState) {
		XFuture<Void> future = fireSendToSM(stream, payload, keepDelayedState);
				
		return future.thenCompose(v -> {
					//if no exceptions occurred, send it on to flow control layer
					if(payload instanceof DataFrame)
						return remoteFlowControl.sendDataToSocket(stream, (DataFrame) payload);
					
					return remoteFlowControl.sendPayloadToSocket(stream, payload);
				});
	}

	public XFuture<Void> firePriorityToClient(PriorityFrame frame) {
		Stream stream;
		try {
			stream = streamState.getStream(frame, true);
		} catch(ConnectionException e) {
			//per spec, priority frames can be received on closed stream but ignore it
			return XFuture.completedFuture(null);
		}
		
		return fireRecvToSM(stream, frame)
				.thenCompose(v -> {
					return localFlowControl.firePriorityToClient(stream, frame);
				});
	}

	protected abstract XFuture<Void> sendTrailersToApp(Http2Trailers payload);
	protected abstract XFuture<Void> sendDataToApp(DataFrame payload);

	public XFuture<Void> sendTrailersToAppImpl(Http2Trailers frame, boolean isConnectionError) {
		Stream stream = streamState.getStream(frame, isConnectionError);
		return fireRecvToSM(stream, frame)
				.thenCompose(v -> {
					return localFlowControl.fireDataToClient(stream, frame);
				});
	}
		
	protected XFuture<Void> sendDataToAppImpl(DataFrame payload, boolean isConnectionError) {
		Stream stream = streamState.getStream(payload, isConnectionError);
		return fireRecvToSM(stream, payload)
				.thenCompose(v -> {
					return localFlowControl.fireDataToClient(stream, payload);
				});
	}

	public XFuture<Void> sendUnkownFrame(UnknownFrame msg) {
		Stream stream = streamState.getStream(msg, false);
		return fireRecvToSM(stream, msg)
				.thenCompose(v -> {
					return localFlowControl.fireDataToClient(stream, msg);
				});
	}
	
	public XFuture<Void> connectionWindowUpdate(WindowUpdateFrame msg) {
		return remoteFlowControl.updateConnectionWindowSize(msg);
	}

	public XFuture<Void> streamWindowUpdate(WindowUpdateFrame msg) {
		Stream stream = streamState.getStream(msg, true);
		if(stream == null)
			return XFuture.completedFuture(null);
		
		return remoteFlowControl.updateStreamWindowSize(stream, msg);
	}


}
