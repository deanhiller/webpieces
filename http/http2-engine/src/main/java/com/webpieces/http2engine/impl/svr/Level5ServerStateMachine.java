package com.webpieces.http2engine.impl.svr;

import org.webpieces.javasm.api.State;

import com.webpieces.http2engine.impl.shared.Http2Event;
import com.webpieces.http2engine.impl.shared.Http2Event.Http2SendRecieve;
import com.webpieces.http2engine.impl.shared.Http2PayloadType;
import com.webpieces.http2engine.impl.shared.Level5AbstractStateMachine;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;

public class Level5ServerStateMachine extends Level5AbstractStateMachine {

	public Level5ServerStateMachine(String id, Level6RemoteFlowControl remoteFlowControl,
			Level6LocalFlowControl localFlowControl) {
		super(id, remoteFlowControl, localFlowControl);
		
		State reservedLocal = stateMachine.createState("Reserved(local)");
		State halfClosedRemote = stateMachine.createState("Half Closed(remote)");
	
		NoTransitionImpl failIfNoTransition = new NoTransitionImpl(true);
		NoTransitionImpl streamErrorNoTransition = new NoTransitionImpl(false);
		idleState.addNoTransitionListener(failIfNoTransition);
		openState.addNoTransitionListener(failIfNoTransition);
		closed.addNoTransitionListener(failIfNoTransition);
		reservedLocal.addNoTransitionListener(failIfNoTransition);
		halfClosedRemote.addNoTransitionListener(streamErrorNoTransition);
		
		Http2Event sentHeadersNoEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.HEADERS);
		Http2Event sentHeadersEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.HEADERS_WITH_EOS);
		
		Http2Event recvHeadersNoEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS);
		Http2Event recvHeadersEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS_WITH_EOS);

		Http2Event sentPushPromise = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.PUSH_PROMISE);
		Http2Event sentResetStream = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.RESET_STREAM);		
		Http2Event recvResetStream = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.RESET_STREAM);
		
		Http2Event dataSendNoEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.DATA);
		Http2Event dataSendEos = new Http2Event(Http2SendRecieve.SEND, Http2PayloadType.DATA_WITH_EOS);
		
		Http2Event dataRecvNoEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA);
		Http2Event dataRecvEos = new Http2Event(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA_WITH_EOS);

		stateMachine.createTransition(idleState, openState, recvHeadersNoEos);
		stateMachine.createTransition(idleState, halfClosedRemote, recvHeadersEos); //jump to half closed as is send H AND send ES
		stateMachine.createTransition(idleState, reservedLocal, sentPushPromise);
		
		stateMachine.createTransition(openState, openState, dataRecvNoEos);
		stateMachine.createTransition(openState, halfClosedRemote, dataRecvEos, recvHeadersEos);
		stateMachine.createTransition(openState, closed, sentResetStream, recvResetStream);
		
		stateMachine.createTransition(reservedLocal, halfClosedRemote, sentHeadersNoEos);
		stateMachine.createTransition(reservedLocal, closed, sentHeadersEos, sentResetStream, recvResetStream);
		
		stateMachine.createTransition(halfClosedRemote, closed, sentHeadersEos, dataSendEos, recvResetStream, sentResetStream);
		stateMachine.createTransition(halfClosedRemote, halfClosedRemote, sentHeadersNoEos, dataSendNoEos);
	}

}
