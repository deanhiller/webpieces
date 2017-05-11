package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.client.ClientStreamWriter;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2engine.impl.RequestWriterImpl;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level3AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level5LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level5RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.PermitQueue;

public class Level3ClientStreams extends Level3AbstractStreamMgr {

	private static final Logger log = LoggerFactory.getLogger(Level3ClientStreams.class);
	private Level4ClientStateMachine clientSm;

	private HeaderSettings localSettings;
	private int permitCount;
	private PermitQueue<ClientStreamWriter> permitQueue;

	//purely for logging!!!  do not use for something else
	private AtomicInteger acquiredCnt = new AtomicInteger(0);
	private AtomicInteger releasedCnt = new AtomicInteger(0);
	private int afterResetExpireSeconds;

	public Level3ClientStreams(
			StreamState state,
			Level4ClientStateMachine clientSm, 
			Level5LocalFlowControl localFlowControl,
			Level5RemoteFlowControl level5FlowControl,
			Http2Config config,
			HeaderSettings remoteSettings
	) {
		super(level5FlowControl, localFlowControl, remoteSettings, state);
		this.clientSm = clientSm;
		this.localSettings = config.getLocalSettings();
		this.remoteSettings = remoteSettings;
		this.permitCount = config.getInitialRemoteMaxConcurrent();
		afterResetExpireSeconds = config.getAfterResetExpireSeconds();
		permitQueue = new PermitQueue<>(config.getInitialRemoteMaxConcurrent());
	}

	public CompletableFuture<ClientStreamWriter> createStreamAndSend(Http2Headers frame, Http2ResponseListener responseListener) {
		if(closedReason != null) {
			log.info("returning CompletableFuture.exception since this socket is closed(or closing)");
			CompletableFuture<ClientStreamWriter> future = new CompletableFuture<>();
			future.completeExceptionally(new ConnectionClosedException("Connection closed or closing", closedReason));
			return future;
		}
		return permitQueue.runRequest(() -> createStreamSendImpl(frame, responseListener));
	}

	private CompletableFuture<ClientStreamWriter> createStreamSendImpl(Http2Headers frame, Http2ResponseListener responseListener) {
		int val = acquiredCnt.incrementAndGet();
		log.info("got permit(cause="+frame+").  size="+permitQueue.availablePermits()+" acquired="+val);
		
		Stream stream = createStream(frame.getStreamId(), responseListener, null);
		return clientSm.fireToSocket(stream, frame)
				.thenApply(s -> new RequestWriterImpl(stream, this));
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
	
	@Override
	protected CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		return fireToSocket(stream, frame, true);
	}
	
	private CompletableFuture<Void> fireToSocket(Stream stream, PartialStream frame, boolean keepDelayedState) {
		return clientSm.fireToSocket(stream, frame).thenApply(v -> {
			checkForClosedState(stream, frame, keepDelayedState);
			return null;
		});
	}
	
	private void checkForClosedState(Stream stream, PartialStream cause, boolean keepDelayedState) {
		//If a stream ends up in closed state, for request type streams, we must release a permit on
		//max concurrent streams
		boolean isClosed = clientSm.isInClosedState(stream);
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
		
		//request stream, so increase permits
		permitQueue.releasePermit();
		int val = releasedCnt.decrementAndGet();
		log.info("release permit(cause="+cause+").  size="+permitQueue.availablePermits()+" releasedCnt="+val);
		return; //we closed the stream
	}
	
	private Stream createStream(int streamId, Http2ResponseListener responseListener, PushPromiseListener pushListener) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		Stream stream = new Stream(streamId, initialState, responseListener, pushListener, localWindowSize, remoteWindowSize);
		return streamState.create(stream);
	}

	@Override
	public CompletableFuture<Void> sendPriorityFrame(PriorityFrame frame) {
		if(closedReason != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		Stream stream;
		try {
			stream = streamState.getStream(frame);
		} catch(ConnectionException e) {
			//per spec, priority frames can be received on closed stream but ignore it
			return CompletableFuture.completedFuture(null);
		}			
		
		return clientSm.fireToClient(stream, frame, null)
						.thenApply(s -> null);

	}
	
	@Override
	public CompletableFuture<Void> sendPayloadToApp(PartialStream frame) {
		if(closedReason != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		if(frame instanceof Http2Push) {
			return sendPushPromiseToClient((Http2Push) frame);
		}
		
		Stream stream = streamState.getStream(frame);
		if(stream == null) {
			//RstStreamFrame only...
			return CompletableFuture.completedFuture(null);
		}
		
		return clientSm.fireToClient(stream, frame, () -> checkForClosedState(stream, frame, false))
					.thenApply(s -> null);
	}
		
	public CompletableFuture<Void> sendPushPromiseToClient(Http2Push fullPromise) {		
		if(closedReason != null) {
			log.info("ignoring incoming push="+fullPromise+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		int newStreamId = fullPromise.getPromisedStreamId();
		if(newStreamId % 2 == 1)
			throw new ConnectionException(ParseFailReason.INVALID_STREAM_ID, newStreamId, 
					"Server sent bad push promise="+fullPromise+" as new stream id is incorrect and is an odd number");

		Stream causalStream = streamState.getStream(fullPromise);
		
		Http2ResponseListener listener = causalStream.getResponseListener();
		PushPromiseListener pushListener = listener.newIncomingPush(newStreamId);

		Stream stream = createStream(newStreamId, null, pushListener);
		return clientSm.fireToClient(stream, fullPromise, null).thenApply(s -> null);
	}

	@Override
	protected void modifyMaxConcurrentStreams(long value) {
		if(value == permitCount)
			return;
		else if (value > Integer.MAX_VALUE)
			throw new IllegalArgumentException("remote setting too large");

		int modifyPermitsCnt = (int) (value - permitCount);
		permitQueue.modifyPermitPoolSize(modifyPermitsCnt);
	}

}
