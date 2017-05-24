package com.webpieces.http2engine.impl.svr;

import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_DATA;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_HEADERS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_RST;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_DATA;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_HEADERS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_PUSH;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_RST;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.State;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.impl.shared.Level5AbstractStateMachine;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;

public class Level5ServerStateMachine extends Level5AbstractStateMachine {

	private Level6SvrLocalFlowControl local;

	public Level5ServerStateMachine(String id, Level6RemoteFlowControl remoteFlowControl,
			Level6SvrLocalFlowControl localFlowControl) {
		super(id, remoteFlowControl, localFlowControl);
		local = localFlowControl;
		
		State reservedLocal = stateMachine.createState("Reserved(local)");
	
		NoTransitionImpl failIfNoTransition = new NoTransitionImpl(true);
		NoTransitionImpl streamErrorNoTransition = new NoTransitionImpl(false);
		idleState.addNoTransitionListener(failIfNoTransition);
		openState.addNoTransitionListener(failIfNoTransition);
		closed.addNoTransitionListener(streamErrorNoTransition);
		reservedLocal.addNoTransitionListener(failIfNoTransition);
		halfClosedRemote.addNoTransitionListener(streamErrorNoTransition);
		
		stateMachine.createTransition(idleState, openState, RECV_HEADERS);
		stateMachine.createTransition(idleState, halfClosedRemote, RECV_HEADERS_EOS); //jump to half closed as is send H AND send ES
		stateMachine.createTransition(idleState, reservedLocal, SENT_PUSH);
		
		stateMachine.createTransition(openState, openState, RECV_DATA, SENT_DATA, SENT_HEADERS);
		stateMachine.createTransition(openState, halfClosedRemote, RECV_DATA_EOS, RECV_HEADERS_EOS);
		stateMachine.createTransition(openState, halfClosedLocal, SENT_DATA_EOS, SENT_HEADERS_EOS);
		stateMachine.createTransition(openState, closed, SENT_RST, RECV_RST);
		
		stateMachine.createTransition(reservedLocal, halfClosedRemote, SENT_HEADERS);
		stateMachine.createTransition(reservedLocal, closed, SENT_HEADERS_EOS, SENT_RST, RECV_RST);
		
		stateMachine.createTransition(halfClosedRemote, halfClosedRemote, SENT_HEADERS, SENT_DATA);

		stateMachine.createTransition(halfClosedLocal, halfClosedLocal, RECV_DATA); //only trailing headers allowed (ie. must have EOS)

	}

	public CompletableFuture<State> fireToClient(ServerStream stream, Http2Request payload) {
		State result = fireToClientImpl(stream, payload);
		return local.fireHeadersToClient(stream, payload)
				.thenApply( v -> result);
	}
}
