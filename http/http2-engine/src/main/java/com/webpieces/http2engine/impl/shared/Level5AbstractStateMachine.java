package com.webpieces.http2engine.impl.shared;

import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_RST;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_RST;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.impl.shared.data.Http2Event;
import com.webpieces.http2engine.impl.shared.data.Http2Event.Http2SendRecieve;
import com.webpieces.http2engine.impl.shared.data.Http2PayloadType;
import com.webpieces.http2engine.impl.shared.data.NoTransitionConnectionError;
import com.webpieces.http2engine.impl.shared.data.NoTransitionStreamError;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.ParseFailReason;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level5AbstractStateMachine {

	private static final Logger log = LoggerFactory.getLogger(Level5AbstractStateMachine.class);

	private Level6RemoteFlowControl remoteFlowControl;
	protected Level6LocalFlowControl localFlowControl;

	protected StateMachine stateMachine;
	protected State idleState;
	protected State closed;
	protected State openState;
	protected State halfClosedLocal;
	protected State halfClosedRemote;

	public Level5AbstractStateMachine(String id, Level6RemoteFlowControl remoteFlowControl, Level6LocalFlowControl localFlowControl) {
		this.remoteFlowControl = remoteFlowControl;
		this.localFlowControl = localFlowControl;
		
		StateMachineFactory factory = StateMachineFactory.createFactory();
		stateMachine = factory.createStateMachine(id);

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

	public CompletableFuture<Void> fireToSocket(Stream stream, Http2Msg payload) {
		Memento state = stream.getCurrentState();
		Http2Event event = translate(Http2SendRecieve.SEND, payload);

		log.info("state before event="+state.getCurrentState()+" event="+event);
		State result = stateMachine.fireEvent(state, event);
		
		log.info("state after="+result);
		//if no exceptions occurred, send it on to flow control layer
		return remoteFlowControl.sendPayloadToSocket(stream, payload);
	}
	
	public CompletableFuture<Void> fireToSocket(Stream stream, PartialStream payload) {
		Memento state = stream.getCurrentState();
		Http2Event event = translate(Http2SendRecieve.SEND, payload);

		log.info("state before event="+state.getCurrentState()+" event="+event);
		State result = stateMachine.fireEvent(state, event);
		
		log.info("state after="+result);
		//if no exceptions occurred, send it on to flow control layer
		if(payload instanceof DataFrame)
			return remoteFlowControl.sendDataToSocket(stream, (DataFrame) payload);
		
		return remoteFlowControl.sendPayloadToSocket(stream, payload);
	}

	public CompletableFuture<State> firePriorityToClient(Stream stream, PriorityFrame payload) {
		State result = fireToClientImpl(stream, payload);
		return localFlowControl.firePriorityToClient(stream, payload)
				.thenApply( v -> result);
	}
	
	public CompletableFuture<State> fireToClient(Stream stream, Http2Trailers payload) { //, Supplier<StreamTransition> possiblyClose
		State state = fireToClientImpl(stream, payload);
		//StreamTransition streamTransition = possiblyClose.get();
		return localFlowControl.fireHeadersToClient(stream, payload)
				.thenApply( v -> state);
	}

	public CompletableFuture<State> fireToClient(Stream stream, RstStreamFrame payload) { //, Supplier<StreamTransition> possiblyClose
		State state = fireToClientImpl(stream, payload);
		//StreamTransition streamTransition = possiblyClose.get();
		return localFlowControl.fireRstToClient(stream, payload)
				.thenApply( v -> state);
	}
	
	public CompletableFuture<State> fireToClient(Stream stream, PartialStream payload) { //, Supplier<StreamTransition> possiblyClose
		State state = fireToClientImpl(stream, payload);
		//StreamTransition streamTransition = possiblyClose.get();
		return localFlowControl.fireDataToClient(stream, payload)
				.thenApply( v -> state);
	}

	protected State fireToClientImpl(Stream stream, Http2Msg payload) {
		try {
			return fireToClientImpl2(stream, payload);
		} catch(NoTransitionConnectionError t) {
			throw new ConnectionException(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, stream.getStreamId(), t.getMessage(), t);
		} catch(NoTransitionStreamError t) {
			throw new StreamException(ParseFailReason.CLOSED_STREAM, stream.getStreamId(), t.getMessage(), t);				
		}
	}
	
	private State fireToClientImpl2(Stream stream, Http2Msg payload) {
		Memento currentState = stream.getCurrentState();
		Http2Event event = translate(Http2SendRecieve.RECEIVE, payload);
		
		log.info("firing event to new statemachine="+event+" state="+currentState.getCurrentState());
		State result = stateMachine.fireEvent(currentState, event);
		log.info("done firing.  new state="+result);
		return result;
	}
	
	public boolean isInClosedState(Stream stream) {
		State currentState = stream.getCurrentState().getCurrentState();
		if(currentState == closed)
			return true;
		return false;
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
		} else if(payload instanceof RstStreamFrame) {
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
