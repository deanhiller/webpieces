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
		
		Http2Event sentHeadersNoEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.HEADERS);
		Http2Event sentHeadersEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.HEADERS_WITH_EOS);
		
		Http2Event recvHeadersNoEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS);
		Http2Event recvHeadersEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS_WITH_EOS);

		Http2Event recvPushPromise = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.PUSH_PROMISE);
		Http2Event sentResetStream = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.RESET_STREAM);		
		Http2Event recvResetStream = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.RESET_STREAM);
		
		Http2Event dataSendNoEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.DATA);
		Http2Event dataSendEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.DATA_WITH_EOS);
		
		Http2Event dataRecvNoEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA);
		Http2Event dataRecvEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA_WITH_EOS);

		stateMachine.createTransition(idleState, openState, sentHeadersNoEos);
		stateMachine.createTransition(idleState, halfClosedLocal, sentHeadersEos); //jump to half closed as is send H AND send ES
		stateMachine.createTransition(idleState, reservedState, recvPushPromise);
		
		stateMachine.createTransition(openState, openState, dataSendNoEos);
		stateMachine.createTransition(openState, halfClosedLocal, dataSendEos);
		stateMachine.createTransition(openState, closed, sentResetStream, recvResetStream);
		
		stateMachine.createTransition(reservedState, halfClosedLocal, recvHeadersNoEos);
		stateMachine.createTransition(reservedState, closed, recvHeadersEos, sentResetStream, recvResetStream);
		
		stateMachine.createTransition(halfClosedLocal, closed, dataRecvEos, recvResetStream, sentResetStream);
		stateMachine.createTransition(halfClosedLocal, halfClosedLocal, recvHeadersNoEos, dataRecvNoEos);
	}

}
