package com.webpieces.http2engine.impl.shared;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2engine.impl.CachedRequest;
import com.webpieces.http2engine.impl.RequestWriterImpl;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level3StreamInitialization {

	private static final Logger log = LoggerFactory.getLogger(Level3StreamInitialization.class);
	private Level4ClientStateMachine clientSm;
	private StreamState streamState;
	private Level5RemoteFlowControl level5FlowControl;
	private HeaderSettings localSettings;
	private HeaderSettings remoteSettings;
	private int startingMaxConcurrent = 50;
	private Semaphore newStreamPermits = new Semaphore(startingMaxConcurrent); //start with 100 until we get word from remote endpoint
	private LinkedList<CachedRequest> queue = new LinkedList<>();
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
	}

	public synchronized CompletableFuture<StreamWriter> createStreamAndSend(Http2Headers frame, Http2ResponseListener responseListener) {
		//always queue the request first...(this creates fairness in first in first out sending to server)
		CompletableFuture<StreamWriter> future = queueRequest(frame, responseListener);
		
		processItemFromQueue();

		log.info("queue size="+queue.size()+" currently running="+(startingMaxConcurrent-newStreamPermits.availablePermits()));
		return future;
	}

	private void processItemFromQueue() {
		CachedRequest req;
		synchronized(this) {
			CachedRequest peek = queue.peek();
			if(peek == null)
				return;
		
			boolean acquired = newStreamPermits.tryAcquire();
			if(!acquired) 
				return;
			
			req = queue.poll();
		}
		
		Http2Headers frame = req.getFrame();

		int val = acquiredCnt.incrementAndGet();
		log.info("got permit(cause="+frame+").  size="+newStreamPermits.availablePermits()+" acquired="+val);
		
		Http2ResponseListener responseListener = req.getResponseListener();
		CompletableFuture<StreamWriter> future = req.getFuture();
		
		Stream stream = createStream(frame.getStreamId(), responseListener, null);
		clientSm.fireToSocket(stream, frame).handle((v, e) -> handleCompletion(v, e, future, stream));
	}

	private Void handleCompletion(Void v, Throwable t, CompletableFuture<StreamWriter> future, Stream stream) {
		if(t != null) {
			future.completeExceptionally(new RuntimeException(t));
			return null;
		}
		
		future.complete(new RequestWriterImpl(stream, this));
		return null;
	}
	
	public CompletableFuture<Void> sendMoreStreamData(Stream stream, PartialStream data) {
		CompletableFuture<Void> future = clientSm.fireToSocket(stream, data);
		checkForClosedState(stream, data);
		return future;
	}
	
	private void checkForClosedState(Stream stream, PartialStream cause) {
		//If a stream ends up in closed state, for request type streams, we must release a permit on
		//max concurrent streams
		boolean isClosed = clientSm.isInClosedState(stream);
		if(!isClosed)
			return; //do nothing
		
		log.info("stream closed="+stream.getStreamId());
		Stream removedStream = streamState.remove(stream);
		if(removedStream == null)
			return; //someone else beat us to it so just return
		
		if(stream.getStreamId() % 2 == 1) {
			//request stream, so increase permits
			this.newStreamPermits.release();
			int val = releasedCnt.decrementAndGet();
			log.info("release permit(cause="+cause+").  size="+newStreamPermits.availablePermits()+" releasedCnt="+val);
			processItemFromQueue(); //process any items from queue now that there is a new permit
		}
	}

	private CompletableFuture<StreamWriter> queueRequest(Http2Headers frame, Http2ResponseListener responseListener) {
		CompletableFuture<StreamWriter> future = new CompletableFuture<StreamWriter>();
		queue.add(new CachedRequest(frame, responseListener, future));
		return future;
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
