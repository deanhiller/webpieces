package com.webpieces.http2engine.impl.client;

import org.webpieces.javasm.api.State;

import com.webpieces.http2engine.impl.shared.Http2Event;
import com.webpieces.http2engine.impl.shared.Http2Event.Http2SendRecieve;
import com.webpieces.http2engine.impl.shared.Http2PayloadType;
import com.webpieces.http2engine.impl.shared.Level4AbstractStateMachine;
import com.webpieces.http2engine.impl.shared.Level5LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level5RemoteFlowControl;

public class Level4ClientStateMachine extends Level4AbstractStateMachine {

	public Level4ClientStateMachine(String id,
			Level5RemoteFlowControl remoteFlowControl, 
			Level5LocalFlowControl localFlowControl
	) {
		super(id, remoteFlowControl, localFlowControl);
		
		State openState = stateMachine.createState("Open");
		State reservedState = stateMachine.createState("Reserved(remote)");
		State halfClosedLocal = stateMachine.createState("Half Closed(local)");
	
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

}
