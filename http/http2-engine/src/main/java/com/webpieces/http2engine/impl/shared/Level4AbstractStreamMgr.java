package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2engine.api.server.StreamReference;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.StreamException;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level4AbstractStreamMgr {

	private final static Logger log = LoggerFactory.getLogger(Level4AbstractStreamMgr.class);
	protected StreamState streamState;
	protected HeaderSettings remoteSettings;
	private Level6RemoteFlowControl remoteFlowControl;
	private Level6LocalFlowControl localFlowControl;
	protected ConnectionException closedReason;
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
	protected abstract CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame);

	public CompletableFuture<Void> sendRstToServerAndClient(StreamException e) {
		if(closedReason != null) {
			log.info("ignoring incoming reset since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		RstStreamFrame frame = new RstStreamFrame();
		frame.setKnownErrorCode(e.getReason().getErrorCode());
		frame.setStreamId(e.getStreamId());
		
		Stream stream = streamState.getStream(frame);
		return fireRstToSocket(stream, frame)
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
			StreamReference writer = stream.getStreamWriter();
			if(writer != null)
				fireReset(e, (c) -> writer.cancel(c).thenApply((w) -> null));
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
			if(stream != null)
				return remoteFlowControl.updateStreamWindowSize(stream, msg);
			return CompletableFuture.completedFuture(null);
		}
	}

	public CompletableFuture<Void> sendMoreStreamData(Stream stream, PartialStream data) {
		if(closedReason != null) {
			log.info("returning CompletableFuture.exception since this socket is closed(or closing)");
			CompletableFuture<Void> future = new CompletableFuture<>();
			future.completeExceptionally(new ConnectionClosedException("Connection closed or closing", closedReason));
			return future;
		}
		
		return fireToSocket(stream, data, false);
	}
	
	protected CompletableFuture<Void> fireToSocket(Stream stream, PartialStream frame, boolean keepDelayedState) {
		return stateMachine.fireToSocket(stream, frame).thenApply(v -> {
			checkForClosedState(stream, frame, keepDelayedState);
			return null;
		});
	}
	
	protected void checkForClosedState(Stream stream, PartialStream cause, boolean keepDelayedState) {
		//If a stream ends up in closed state, for request type streams, we must release a permit on
		//max concurrent streams
		boolean isClosed = stateMachine.isInClosedState(stream);
		if(!isClosed)
			return; //do nothing
		
		log.info("stream closed="+stream.getStreamId());
		
		if(!keepDelayedState) {
			Stream removedStream = streamState.remove(stream);
			if(removedStream == null)
				return; //someone else closed the stream. they beat us to it so just return
		} else {
			//streamState.addDelayedRemove(stream, afterResetExpireSeconds);
			throw new UnsupportedOperationException("not supported");
		}
		
		release(cause);

		return; //we closed the stream
	}
	
	protected abstract void release(PartialStream cause);

	public void setMaxConcurrentStreams(long value) {
		remoteSettings.setMaxConcurrentStreams(value);
		
		modifyMaxConcurrentStreams(value);
	}

	protected abstract void modifyMaxConcurrentStreams(long value);

	public abstract CompletableFuture<Void> sendPriorityFrame(PriorityFrame msg);


}
