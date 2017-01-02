package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;

import com.webpieces.http2engine.api.dto.Http2Headers;
import com.webpieces.http2engine.api.dto.Http2Push;
import com.webpieces.http2engine.api.dto.PartialStream;
import com.webpieces.http2engine.impl.Http2Event.Http2SendRecieve;
import com.webpieces.http2parser.api.dto.DataFrame;

public class Level4ClientStateMachine {

	private StateMachine stateMachine;
	private State idleState;
	private Level5FlowControl flowControl;
	
	public Level4ClientStateMachine(String id, Level5FlowControl flowControl) {
		this.flowControl = flowControl;
		StateMachineFactory factory = StateMachineFactory.createFactory();
		stateMachine = factory.createStateMachine(id);

		idleState = stateMachine.createState("idle");
		State openState = stateMachine.createState("Open");
		State reservedState = stateMachine.createState("Reserved(remote)");
		State halfClosedLocal = stateMachine.createState("Half Closed(local)");
		State closed = stateMachine.createState("closed");
	
		NoTransitionImpl failIfNoTransition = new NoTransitionImpl();
		idleState.addNoTransitionListener(failIfNoTransition);
		openState.addNoTransitionListener(failIfNoTransition);
		reservedState.addNoTransitionListener(failIfNoTransition);
		halfClosedLocal.addNoTransitionListener(failIfNoTransition);
		closed.addNoTransitionListener(failIfNoTransition);
		
		Http2Event sentHeaders = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.HEADERS);
		Http2Event recvPushPromise = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.PUSH_PROMISE);
		Http2Event sentEndStreamFlag = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.END_STREAM_FLAG);
		Http2Event sentResetStream = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.RESET_STREAM);
		Http2Event recvHeaders = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS);
		Http2Event receivedResetStream = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.RESET_STREAM);
		Http2Event recvEndStreamFlag = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.END_STREAM_FLAG);
		Http2Event dataSend = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.DATA);
		Http2Event dataRecv = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA);
		
		stateMachine.createTransition(idleState, openState, sentHeaders);
		stateMachine.createTransition(idleState, reservedState, recvPushPromise);
		stateMachine.createTransition(openState, halfClosedLocal, sentEndStreamFlag);
		stateMachine.createTransition(openState, closed, sentResetStream, receivedResetStream);		
		stateMachine.createTransition(reservedState, halfClosedLocal, recvHeaders);
		stateMachine.createTransition(reservedState, closed, sentResetStream, receivedResetStream);
		stateMachine.createTransition(halfClosedLocal, closed, recvEndStreamFlag, receivedResetStream, sentResetStream);
		
		//extra transitions defined such that we can catch unknown transitions
		stateMachine.createTransition(openState, openState, dataSend, sentHeaders);

		stateMachine.createTransition(halfClosedLocal, halfClosedLocal, recvHeaders, dataRecv);
		
		
	}

	private static class NoTransitionImpl implements NoTransitionListener {
		@Override
		public void noTransitionFromEvent(State state, Object event) {
			throw new RuntimeException("No transition defined on statemachine for event="+event+" when in state="+state);
		}
	}
	
	public CompletableFuture<Void> fireToSocket(Memento currentState, PartialStream payload) {
		Http2PayloadType payloadType = translate(payload);
		Http2Event event = new Http2Event(Http2SendRecieve.SEND, payloadType);

		stateMachine.fireEvent(currentState, event);
		
		//sometimes a single frame has two events :( per http2 spec
		if(payload.isEndOfStream())
			stateMachine.fireEvent(currentState, new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.END_STREAM_FLAG));
		
		//if no exceptions occurred, send it on to flow control layer
		return flowControl.sendPayloadToSocket(payload);
	}
	
	public Memento createStateMachine(String streamId) {
		return stateMachine.createMementoFromState("stream"+streamId, idleState);
	}
	
	private Http2PayloadType translate(PartialStream payload) {
		if(payload instanceof Http2Headers) {
			return Http2PayloadType.HEADERS;
		} else if(payload instanceof DataFrame) {
			return Http2PayloadType.DATA;
		} else if(payload instanceof Http2Push) {
			return Http2PayloadType.PUSH_PROMISE;
		} else
			throw new IllegalArgumentException("unknown payload type for payload="+payload);
	}

	public void testStateMachineTransition(Memento currentState, PartialStream payload) {
		Http2PayloadType payloadType = translate(payload);
		Http2Event event = new Http2Event(Http2SendRecieve.RECEIVE, payloadType);
		
		stateMachine.fireEvent(currentState, event);
		
		if(payload.isEndOfStream())
			stateMachine.fireEvent(currentState, new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.END_STREAM_FLAG)); //validates state transition is ok
		
		//if no exceptions occurred, return and data is sent on to client
	}

}
