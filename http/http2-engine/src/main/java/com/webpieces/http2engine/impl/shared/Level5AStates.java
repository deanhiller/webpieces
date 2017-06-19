package com.webpieces.http2engine.impl.shared;

import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_RST;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_RST;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;
import org.webpieces.util.locking.AsyncLock;
import org.webpieces.util.locking.PermitQueue;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.impl.shared.data.Http2Event;
import com.webpieces.http2engine.impl.shared.data.Http2Event.Http2SendRecieve;
import com.webpieces.http2engine.impl.shared.data.Http2PayloadType;
import com.webpieces.http2engine.impl.shared.data.NoTransitionConnectionError;
import com.webpieces.http2engine.impl.shared.data.NoTransitionStreamError;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Level5AStates {

	private final static Logger log = LoggerFactory.getLogger(Level5AStates.class);

	protected AsyncLock asyncLock;
	protected StateMachine stateMachine;
	protected State idleState;
	protected State closed;
	protected State openState;
	protected State halfClosedLocal;
	protected State halfClosedRemote;
	
	protected StreamState streamState;

	private PermitQueue maxConcurrentQueue;
	//purely for logging!!!  do not use for something else
	private AtomicInteger releasedCnt = new AtomicInteger(0);

	protected String logId;

	public Level5AStates(String key, StreamState streamState, PermitQueue maxConcurrentQueue) {
		this.logId = key;
		this.maxConcurrentQueue = maxConcurrentQueue;
		asyncLock = new AsyncLock(key+"SM");
		this.streamState = streamState;
		StateMachineFactory factory = StateMachineFactory.createFactory();
		stateMachine = factory.createStateMachine(key);

		//shared states..
		idleState = stateMachine.createState("idle");
		openState = stateMachine.createState("Open");
		closed = stateMachine.createState("closed");
		halfClosedLocal = stateMachine.createState("Half Closed(local)");
		halfClosedRemote = stateMachine.createState("Half Closed(remote)");

		//shared transitions..
		stateMachine.createTransition(halfClosedLocal, closed, RECV_HEADERS_EOS, RECV_DATA_EOS, RECV_RST, SENT_RST);
		
		stateMachine.createTransition(halfClosedRemote, closed, SENT_HEADERS_EOS, SENT_DATA_EOS, RECV_RST, SENT_RST);
	}

	protected CompletableFuture<Void> fireSendToSM(Stream stream, Http2Msg payload, boolean keepDelayedState) {
		return fireToStateMachine(Http2SendRecieve.SEND, stream, payload, keepDelayedState);
	}
	protected CompletableFuture<Void> fireRecvToSM(Stream stream, Http2Msg payload) {
		return fireToStateMachine(Http2SendRecieve.RECEIVE, stream, payload, false);
	}
	
	private CompletableFuture<Void> fireToStateMachine(Http2SendRecieve type, Stream stream, Http2Msg payload, boolean keepDelayedState) {
		Http2Event event = translate(type, payload);

		CompletableFuture<Void> future = stream.getLock().synchronizeD( () -> {

			fireToStatemachineImpl(stream, event);
			return checkForClosedState(stream, payload, keepDelayedState);
			
		}).thenApply( isReleased -> {
			release(isReleased, stream, payload);
			return null;
		});
		
		return future.handle((v, e) -> {
	        if (e == null) {
	            return CompletableFuture.completedFuture(v);
	        } else {
	        	return translateException(stream, e);
	        }
	    }).thenCompose(Function.identity());
	}

	public void release(boolean isReleased, Stream stream, Object cause) {
		if(isReleased && stream.isHasPermit()) {
			maxConcurrentQueue.releasePermit();
			int val = releasedCnt.incrementAndGet();
			log.trace(() -> "release permit(cause="+cause+").  size="+maxConcurrentQueue.availablePermits()+" releasedCnt="+val+" stream="+stream.getStreamId());
		}
	}
	
	private CompletableFuture<Void> translateException(Stream stream, Throwable t) {
		CompletableFuture<Void> fut = new CompletableFuture<>();
		if(t instanceof NoTransitionConnectionError)
			fut.completeExceptionally(new ConnectionException(CancelReasonCode.BAD_FRAME_RECEIVED_FOR_THIS_STATE, logId, stream.getStreamId(), t.getMessage(), t));
		else if(t instanceof NoTransitionStreamError)
			fut.completeExceptionally(new StreamException(CancelReasonCode.CLOSED_STREAM, logId, stream.getStreamId(), t.getMessage(), t));
		else
			fut.completeExceptionally(t);
		
		return fut;
	}
	
	private void fireToStatemachineImpl(Stream stream, Http2Event event) {
		Memento currentState = stream.getCurrentState();
		State old = currentState.getCurrentState();
		
		State result = stateMachine.fireEvent(currentState, event);
		log.trace(() -> logId+"done firing evt="+event+" "+old+" -> "+result);
	}
	
	public boolean isInClosedState(Stream stream) {
		State currentState = stream.getCurrentState().getCurrentState();
		if(currentState == closed)
			return true;
		return false;
	}
	
	/**
	 * Returns if calling this resulted in closing the stream and cleaning up state
	 */
	private boolean checkForClosedState(Stream stream, Http2Msg cause, boolean keepDelayedState) {
		//If a stream ends up in closed state, for request type streams, we must release a permit on
		//max concurrent streams
		boolean isClosed = isInClosedState(stream);
		if(!isClosed)
			return false; //do nothing
		
		log.trace(() -> logId+"stream closed="+stream.getStreamId());
		
//		if(!keepDelayedState) {
			Stream removedStream = streamState.remove(stream, cause);
			if(removedStream == null)
				return false; //someone else closed the stream. they beat us to it so just return
			return true;
//		} else {
//			//streamState.addDelayedRemove(stream, afterResetExpireSeconds);
//			throw new UnsupportedOperationException("not supported");
//		}
		
	}
	
	public Memento createStateMachine(String streamId) {
		return stateMachine.createMementoFromState("SM"+streamId, idleState);
	}
	
	protected Http2Event translate(Http2SendRecieve sendRecv, Http2Msg payload) {
		Http2PayloadType payloadType;
		if(payload instanceof Http2Headers) {
			Http2Headers head = (Http2Headers) payload;
			if(head.isEndOfStream())
				payloadType = Http2PayloadType.HEADERS_EOS;
			else
				payloadType = Http2PayloadType.HEADERS;
		} else if(payload instanceof DataFrame) {
			DataFrame data = (DataFrame) payload;
			if(data.isEndOfStream())
				payloadType = Http2PayloadType.DATA_EOS;
			else
				payloadType = Http2PayloadType.DATA;
		} else if(payload instanceof Http2Push) {
			payloadType = Http2PayloadType.PUSH_PROMISE;
		} else if(payload instanceof CancelReason) {
			payloadType = Http2PayloadType.RESET_STREAM;
		} else
			throw new IllegalArgumentException("unknown payload type for payload="+payload);
		
		return Http2Event.lookup(sendRecv, payloadType);
	}
	
	protected static class NoTransitionImpl implements NoTransitionListener {
		private boolean isConnectionError;

		public NoTransitionImpl(boolean isConnectionError) {
			this.isConnectionError = isConnectionError;
		}

		@Override
		public void noTransitionFromEvent(State state, Object event) {
			if(isConnectionError)
				throw new NoTransitionConnectionError("No transition defined on statemachine for event="+event+" when in state="+state);
			else
				throw new NoTransitionStreamError("No transition defined on statemachine for event="+event+" when in state="+state);
		}
	}
}
