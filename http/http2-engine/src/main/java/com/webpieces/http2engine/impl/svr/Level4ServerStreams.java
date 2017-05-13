package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level4AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.PermitQueue;

public class Level4ServerStreams extends Level4AbstractStreamMgr {

	private final static Logger log = LoggerFactory.getLogger(Level4ServerStreams.class);

	private Level5ServerStateMachine serverSm;
	private HeaderSettings localSettings;
	private volatile int streamsInProcess = 0;

	private PermitQueue<Void> permitQueue = new PermitQueue<>(100);
	//purely for logging!!!  do not use for something else
	private AtomicInteger acquiredCnt = new AtomicInteger(0);
	private AtomicInteger releasedCnt = new AtomicInteger(0);
	
	public Level4ServerStreams(StreamState streamState, Level5ServerStateMachine serverSm, Level6LocalFlowControl localFlowControl,
			Level6RemoteFlowControl remoteFlowCtrl, HeaderSettings localSettings, HeaderSettings remoteSettings) {
		super(serverSm, remoteFlowCtrl, localFlowControl, remoteSettings, streamState);
		this.serverSm = serverSm;
		this.localSettings = localSettings;
	}

	@Override
	public CompletableFuture<Void> sendPayloadToApp(PartialStream frame) {
		if(frame instanceof Http2Headers && !streamState.isStreamExist(frame)) {
			return incomingHeadersToApp((Http2Headers) frame);
		} else {
			//this copied from client but for server this should not occur and if does is a connection error?
//			if(closedReason != null) {
//				log.info("ignoring incoming frame="+frame+" since socket is shutting down");
//				return CompletableFuture.completedFuture(null);
//			}
			
			Stream stream = streamState.getStream(frame);
			
			return serverSm.fireToClient(stream, frame, () -> checkForClosedState(stream, frame, false))
						.thenApply(s -> null);
		}
	}

	@Override
	public CompletableFuture<Void> sendMoreStreamData(Stream stream, PartialStream data) {
		if(stream.isPushStream() && data instanceof Http2Headers && !stream.isHeadersSent()) {
			stream.setHeadersSent(true);
			return permitQueue.runRequest(() -> {
				int val = acquiredCnt.incrementAndGet();
				log.info("got permit(cause="+data+").  size="+permitQueue.availablePermits()+" acquired="+val);
				return super.fireToSocket(stream, data, false);
			});
		}
		
		return super.sendMoreStreamData(stream, data);
	}
	
	private CompletableFuture<Void> incomingHeadersToApp(Http2Headers msg) {
		Stream stream = createStream(msg.getStreamId());
		return serverSm.fireToClient(stream, msg, null).thenApply(s -> null);
	}
	
	private Stream createStream(int streamId) {
		Memento initialState = serverSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		Stream stream = new Stream(streamId, initialState, null, null, localWindowSize, remoteWindowSize);
		return streamState.create(stream);
	}

	public CompletableFuture<Stream> sendResponseHeaderToSocket(Stream origStream, Http2Headers frame) {
		if(closedReason != null) {
			return createExcepted("sending response headers").thenApply((s) -> null);
		}
		Stream stream = streamState.getStream(frame);
		
		return serverSm.fireToSocket(stream, frame)
				.thenApply(s -> stream);
	}
	
	public CompletableFuture<Stream> sendPush(Http2Push push) {
		int newStreamId = push.getPromisedStreamId();
		Stream stream = createStream(newStreamId);

		return serverSm.fireToSocket(stream, push)
				.thenApply(s -> stream);
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
	public CompletableFuture<Void> sendPriorityFrame(PriorityFrame msg) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	protected CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void release(Stream stream, PartialStream cause) {
		if(stream.isPushStream()) {
			permitQueue.releasePermit();
			int val = releasedCnt.decrementAndGet();
			log.info("release permit(cause="+cause+").  size="+permitQueue.availablePermits()+" releasedCnt="+val+" stream="+stream.getStreamId());
		}
	}

}
