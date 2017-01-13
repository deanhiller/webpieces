package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2engine.impl.RequestWriterImpl;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.PermitQueue;

public class Level3StreamInitialization {

	private static final Logger log = LoggerFactory.getLogger(Level3StreamInitialization.class);
	private Level4ClientStateMachine clientSm;
	private StreamState streamState;
	private Level5RemoteFlowControl level5FlowControl;
	private HeaderSettings localSettings;
	private HeaderSettings remoteSettings;
	private int startingMaxConcurrent = 50;
	private PermitQueue<StreamWriter> permitQueue;
	private AtomicInteger acquiredCnt = new AtomicInteger(0);
	private AtomicInteger releasedCnt = new AtomicInteger(0);

	public Level3StreamInitialization(
			StreamState state,
			Level4ClientStateMachine clientSm, 
			Level5RemoteFlowControl level5FlowControl,
			HeaderSettings localSettings,
			HeaderSettings remoteSettings
	) {
		this.streamState = state;
		this.clientSm = clientSm;
		this.level5FlowControl = level5FlowControl;
		this.localSettings = localSettings;
		this.remoteSettings = remoteSettings;
		permitQueue = new PermitQueue<>(startingMaxConcurrent);
	}

	public CompletableFuture<StreamWriter> createStreamAndSend(Http2Headers frame, Http2ResponseListener responseListener) {
		return permitQueue.runRequest(() -> createStreamSendImpl(frame, responseListener));
	}

	private CompletableFuture<StreamWriter> createStreamSendImpl(Http2Headers frame, Http2ResponseListener responseListener) {
		int val = acquiredCnt.incrementAndGet();
		log.info("got permit(cause="+frame+").  size="+permitQueue.availablePermits()+" acquired="+val);
		
		Stream stream = createStream(frame.getStreamId(), responseListener, null);
		return clientSm.fireToSocket(stream, frame)
				.thenApply(s -> new RequestWriterImpl(stream, this));
	}

	public CompletableFuture<Void> sendMoreStreamData(
			Stream stream, PartialStream data) {
		CompletableFuture<Void> future = clientSm.fireToSocket(stream, data);
		if(checkForClosedState(stream, data)) {
			//request stream, so increase permits
			permitQueue.releasePermit();
			int val = releasedCnt.decrementAndGet();
			log.info("release permit(cause="+data+").  size="+permitQueue.availablePermits()+" releasedCnt="+val);
		}
		return future;
	}
	
	private boolean checkForClosedState(Stream stream, PartialStream cause) {
		//If a stream ends up in closed state, for request type streams, we must release a permit on
		//max concurrent streams
		boolean isClosed = clientSm.isInClosedState(stream);
		if(!isClosed)
			return false; //do nothing
		
		log.info("stream closed="+stream.getStreamId());
		Stream removedStream = streamState.remove(stream);
		if(removedStream == null)
			return false; //someone else closed the stream. they beat us to it so just return
		
		return true; //we closed the stream
	}
	
	private Stream createStream(int streamId, Http2ResponseListener responseListener, PushPromiseListener pushListener) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		Stream stream = new Stream(streamId, initialState, responseListener, pushListener, localWindowSize, remoteWindowSize);
		return streamState.create(stream);
	}

	public void sendPayloadToClient(PartialStream frame) {
		if(frame instanceof Http2Push) {
			sendPushPromiseToClient((Http2Push) frame);
			return;
		}
		
		Stream stream = streamState.get(frame);
		
		clientSm.fireToClient(stream, frame, () -> checkForClosedState(stream, frame));
	}

	public void sendPushPromiseToClient(Http2Push fullPromise) {		
		int newStreamId = fullPromise.getPromisedStreamId();
		if(newStreamId % 2 == 1)
			throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, newStreamId, 
					"Server sent bad push promise="+fullPromise+" as new stream id is incorrect and is an odd number", true);

		Stream causalStream = streamState.get(fullPromise);
		
		Http2ResponseListener listener = causalStream.getResponseListener();
		PushPromiseListener pushListener = listener.newIncomingPush(newStreamId);

		Stream stream = createStream(newStreamId, null, pushListener);
		clientSm.fireToClient(stream, fullPromise, null);
	}

	public void updateWindowSize(WindowUpdateFrame msg) {
		if(msg.getStreamId() == 0) {
			level5FlowControl.updateConnectionWindowSize(msg);
		} else {
			Stream stream = streamState.get(msg);
			level5FlowControl.updateStreamWindowSize(stream, msg);
		}
	}

	public void setMaxConcurrentStreams(long value) {
		remoteSettings.setMaxConcurrentStreams(value);
		
	}
}
