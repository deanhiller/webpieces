package com.webpieces.http2parser2.impl.stateful;

import org.webpieces.javasm.api.Event;
import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;

import com.webpieces.http2parser.api.highlevel.Http2FullHeaders;
import com.webpieces.http2parser.api.highlevel.Http2FullPushPromise;
import com.webpieces.http2parser2.impl.stateful.Http2Event.Http2SendRecieve;

public class ClientStateMachine {
	private static final Http2Event sentHeaders = new Http2Event(Http2SendRecieve.SEND, Http2FullHeaders.class);
	private static final Http2Event recvPushPromise = new Http2Event(Http2SendRecieve.SEND, Http2FullPushPromise.class);
	
	private static final Event recvPushPromise = StateMachineFactory.createEvent("Received Push Promise");
	private static final Event sentEndStreamFlag = StateMachineFactory.createEvent("Send End Stream Flag");
	private static final Event sentResetStream = StateMachineFactory.createEvent("Sent Reset Stream");
	private static final Event recvHeaders = StateMachineFactory.createEvent("Recv Headers");
	private static final Event receivedResetStream = StateMachineFactory.createEvent("Recv Reset Stream");
	private static final Event recvEndStreamFlag = StateMachineFactory.createEvent("Recv End Stream Flag");
	private static final Event dateSend = StateMachineFactory.createEvent("Data Send Flag");

	private StateMachine stateMachine;
	private State idleState;
	
	public ClientStateMachine(String id) {
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
		
		stateMachine.createTransition(idleState, openState, sentHeaders);
		stateMachine.createTransition(idleState, reservedState, recvPushPromise);
		stateMachine.createTransition(openState, halfClosedLocal, sentEndStreamFlag);
		stateMachine.createTransition(openState, closed, sentResetStream, receivedResetStream);		
		stateMachine.createTransition(reservedState, halfClosedLocal, recvHeaders);
		stateMachine.createTransition(reservedState, closed, sentResetStream, receivedResetStream);
		stateMachine.createTransition(halfClosedLocal, closed, recvEndStreamFlag, receivedResetStream, sentResetStream);
		
		//extra transitions defined such that we can catch unknown transitions
		stateMachine.createTransition(openState, openState, dateSend);
	}

	private static class NoTransitionImpl implements NoTransitionListener {
		@Override
		public void noTransitionFromEvent(State state, Event event) {
			throw new RuntimeException("No transition defined on statemachine for event="+event+" when in state="+state);
		}
	}
	
	public void fireSendEvent(Memento currentState, Class payloadType) {
		Http2Event event = new Http2Event(Http2SendRecieve.SEND, payloadType);
		
		stateMachine.fireEvent(currentState, event);
	}
	
	


	public Memento createStateMachine(String streamId) {
		return stateMachine.createMementoFromState("stream"+streamId, idleState);
	}
}
