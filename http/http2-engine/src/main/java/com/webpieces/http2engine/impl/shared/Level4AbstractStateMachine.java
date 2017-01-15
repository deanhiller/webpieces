package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.impl.shared.Http2Event.Http2SendRecieve;
import com.webpieces.http2parser.api.dto.DataFrame;
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
		Http2PayloadType payloadType = translate(payload);
		Http2Event event = new Http2Event(Http2SendRecieve.SEND, payloadType);

		CompletableFuture<State> result = stateMachine.fireEvent(state, event);
		return result.thenCompose( s -> {
					//sometimes a single frame has two events :( per http2 spec
					if(payload.isEndOfStream())
						return stateMachine.fireEvent(state, new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.END_STREAM_FLAG));			
					return CompletableFuture.completedFuture(s);
				}).thenCompose( s -> 
					//if no exceptions occurred, send it on to flow control layer
					remoteFlowControl.sendPayloadToSocket(stream, payload)
				);
	}
	
	public CompletableFuture<State> fireToClient(Stream stream, PartialStream payload, Runnable possiblyModifyState) {
		Memento currentState = stream.getCurrentState();
		Http2PayloadType payloadType = translate(payload);
		Http2Event event = new Http2Event(Http2SendRecieve.RECEIVE, payloadType);
		
		CompletableFuture<State> result = stateMachine.fireEvent(currentState, event);
		return result.thenCompose( s -> {
			if(payload.isEndOfStream())
				return stateMachine.fireEvent(currentState, new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.END_STREAM_FLAG)); //validates state transition is ok
			return CompletableFuture.completedFuture(s);
		}).thenApply( s -> {
			//modifying the stream state should be done BEFORE firing to client as if the stream is closed
			//then this will prevent windowUpdateFrame with increment being sent to a closed stream
			if(possiblyModifyState != null)
				possiblyModifyState.run();
			
			localFlowControl.fireToClient(stream, payload);			
			
			return s;
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
	
	protected Http2PayloadType translate(PartialStream payload) {
		if(payload instanceof Http2Headers) {
			return Http2PayloadType.HEADERS;
		} else if(payload instanceof DataFrame) {
			return Http2PayloadType.DATA;
		} else if(payload instanceof Http2Push) {
			return Http2PayloadType.PUSH_PROMISE;
		} else
			throw new IllegalArgumentException("unknown payload type for payload="+payload);
	}
	

	protected static class NoTransitionImpl implements NoTransitionListener {
		public NoTransitionImpl() {
		}

		@Override
		public void noTransitionFromEvent(State state, Object event) {
			throw new RuntimeException("No transition defined on statemachine for event="+event+" when in state="+state);
		}
	}
}
