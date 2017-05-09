package com.webpieces.http2engine.impl.shared;

import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.impl.shared.Http2Event.Http2SendRecieve;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level4AbstractStateMachine {

	private Level5RemoteFlowControl remoteFlowControl;
	private Level5LocalFlowControl localFlowControl;

	protected StateMachine stateMachine;
	protected State idleState;
	protected State closed;
	
	public Level4AbstractStateMachine(String id, Level5RemoteFlowControl remoteFlowControl, Level5LocalFlowControl localFlowControl) {
		this.remoteFlowControl = remoteFlowControl;
		this.localFlowControl = localFlowControl;
		
		StateMachineFactory factory = StateMachineFactory.createFactory();
		stateMachine = factory.createStateMachine(id);

		idleState = stateMachine.createState("idle");
		closed = stateMachine.createState("closed");
	}

	public CompletableFuture<Void> fireToSocket(Stream stream, PartialStream payload) {
		Memento state = stream.getCurrentState();
		Http2Event event = translate(Http2SendRecieve.SEND, payload);

		CompletableFuture<State> result = stateMachine.fireEvent(state, event);
		return result.thenCompose( s -> 
					//if no exceptions occurred, send it on to flow control layer
					remoteFlowControl.sendPayloadToSocket(stream, payload)
				);
	}
	
	public CompletableFuture<State> fireToClient(Stream stream, PartialStream payload, Runnable possiblyModifyState) {
		Memento currentState = stream.getCurrentState();
		Http2Event event = translate(Http2SendRecieve.RECEIVE, payload);
		
		CompletableFuture<State> result = stateMachine.fireEvent(currentState, event);
		return result.thenApply( s -> {
			//modifying the stream state should be done BEFORE firing to client as if the stream is closed
			//then this will prevent windowUpdateFrame with increment being sent to a closed stream
			if(possiblyModifyState != null)
				possiblyModifyState.run();
			
			localFlowControl.fireToClient(stream, payload);
			
			return s;
		}).exceptionally(t -> {
			if(t instanceof NoTransitionException) {
				throw new ConnectionException(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, stream.getStreamId(), t.getMessage(), t);
			}
			throw new RuntimeException(t);
		});

	}
	
	public boolean isInClosedState(Stream stream) {
		State currentState = stream.getCurrentState().getCurrentState();
		if(currentState == closed)
			return true;
		return false;
	}
	
	public Memento createStateMachine(String streamId) {
		return stateMachine.createMementoFromState("stream"+streamId, idleState);
	}
	
	
	protected Http2Event translate(Http2SendRecieve sendRecv, PartialStream payload) {
		Http2PayloadType payloadType;
		if(payload instanceof Http2Headers) {
			if(payload.isEndOfStream())
				payloadType = Http2PayloadType.HEADERS_WITH_EOS;
			else
				payloadType = Http2PayloadType.HEADERS;
		} else if(payload instanceof DataFrame) {
			if(payload.isEndOfStream())
				payloadType = Http2PayloadType.DATA_WITH_EOS;
			else
				payloadType = Http2PayloadType.DATA;
		} else if(payload instanceof Http2Push) {
			payloadType = Http2PayloadType.PUSH_PROMISE;
		} else if(payload instanceof RstStreamFrame) {
			payloadType = Http2PayloadType.RESET_STREAM;
		} else
			throw new IllegalArgumentException("unknown payload type for payload="+payload);
		
		return new Http2Event(sendRecv, payloadType);
	}
	

	protected static class NoTransitionImpl implements NoTransitionListener {
		public NoTransitionImpl() {
		}

		@Override
		public void noTransitionFromEvent(State state, Object event) {
			throw new NoTransitionException("No transition defined on statemachine for event="+event+" when in state="+state);
		}
	}
}
