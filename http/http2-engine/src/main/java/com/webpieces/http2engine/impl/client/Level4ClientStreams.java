package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level4AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.PermitQueue;

public class Level4ClientStreams extends Level4AbstractStreamMgr {

	private static final Logger log = LoggerFactory.getLogger(Level4ClientStreams.class);
	private Level5ClientStateMachine clientSm;

	private HeaderSettings localSettings;
	private PermitQueue<Stream> permitQueue;

	//purely for logging!!!  do not use for something else
	private AtomicInteger acquiredCnt = new AtomicInteger(0);
	private AtomicInteger releasedCnt = new AtomicInteger(0);
	private int afterResetExpireSeconds;

	public Level4ClientStreams(
			StreamState state,
			Level5ClientStateMachine clientSm, 
			Level6LocalFlowControl localFlowControl,
			Level6RemoteFlowControl level5FlowControl,
			Http2Config config,
			HeaderSettings remoteSettings
	) {
		super(clientSm, level5FlowControl, localFlowControl, remoteSettings, state);
		this.clientSm = clientSm;
		this.localSettings = config.getLocalSettings();
		this.remoteSettings = remoteSettings;
		afterResetExpireSeconds = config.getAfterResetExpireSeconds();
		permitQueue = new PermitQueue<>(config.getInitialRemoteMaxConcurrent());
	}

	public CompletableFuture<Stream> createStreamAndSend(Http2Headers frame, Http2ResponseListener responseListener) {
		if(closedReason != null) {
			return createExcepted("send request headers").thenApply((s) -> null);
		}
		return permitQueue.runRequest(() -> createStreamSendImpl(frame, responseListener));
	}

	private CompletableFuture<Stream> createStreamSendImpl(Http2Headers frame, Http2ResponseListener responseListener) {
		int val = acquiredCnt.incrementAndGet();
		log.info("got permit(cause="+frame+").  size="+permitQueue.availablePermits()+" acquired="+val);
		
		Stream stream = createStream(frame.getStreamId(), responseListener, null);
		return clientSm.fireToSocket(stream, frame)
				.thenApply(s -> stream);
	}
	
	@Override
	protected CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		return fireToSocket(stream, frame, true);
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
			stream = streamState.getStream(frame, true);
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
		
		Stream stream = streamState.getStream(frame, true);
		
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

		Stream causalStream = streamState.getStream(fullPromise, true);
		
		Http2ResponseListener listener = causalStream.getResponseListener();
		PushPromiseListener pushListener = listener.newIncomingPush(newStreamId);

		Stream stream = createStream(newStreamId, null, pushListener);
		return clientSm.fireToClient(stream, fullPromise, null).thenApply(s -> null);
	}

	@Override
	protected void modifyMaxConcurrentStreams(long value) {
		int permitCount = permitQueue.totalPermits();
		if(value == permitCount)
			return;
		else if (value > Integer.MAX_VALUE)
			throw new IllegalArgumentException("remote setting too large");

		int modifyPermitsCnt = (int) (value - permitCount);
		permitQueue.modifyPermitPoolSize(modifyPermitsCnt);
	}

	@Override
	protected void release(Stream stream, PartialStream cause) {
		//request stream, so increase permits
		permitQueue.releasePermit();
		int val = releasedCnt.decrementAndGet();
		log.info("release permit(cause="+cause+").  size="+permitQueue.availablePermits()+" releasedCnt="+val+" stream="+stream.getStreamId());
	}

}
