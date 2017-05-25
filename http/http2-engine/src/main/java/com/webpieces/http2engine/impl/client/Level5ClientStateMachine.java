package com.webpieces.http2engine.impl.client;

import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_DATA;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_HEADERS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_PUSH;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.RECV_RST;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_DATA;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_DATA_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_HEADERS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_HEADERS_EOS;
import static com.webpieces.http2engine.impl.shared.data.Http2Event.SENT_RST;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.State;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.impl.shared.Level5AbstractStateMachine;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level5ClientStateMachine extends Level5AbstractStateMachine {

	private Level6ClntLocalFlowControl local;

	public Level5ClientStateMachine(String id,
			Level6RemoteFlowControl remoteFlowControl, 
			Level6ClntLocalFlowControl localFlowControl
	) {
		super(id, remoteFlowControl, localFlowControl);
		local = localFlowControl;
		
		State reservedRemote = stateMachine.createState("Reserved(remote)");
	
		NoTransitionImpl failIfNoTransition = new NoTransitionImpl(true);
		idleState.addNoTransitionListener(failIfNoTransition);
		openState.addNoTransitionListener(failIfNoTransition);
		reservedRemote.addNoTransitionListener(failIfNoTransition);
		halfClosedLocal.addNoTransitionListener(failIfNoTransition);
		closed.addNoTransitionListener(failIfNoTransition);

		stateMachine.createTransition(idleState, openState, SENT_HEADERS);
		stateMachine.createTransition(idleState, halfClosedLocal, SENT_HEADERS_EOS); //jump to half closed as is send H AND send ES
		stateMachine.createTransition(idleState, reservedRemote, RECV_PUSH);
		
		stateMachine.createTransition(openState, openState, SENT_DATA);
		stateMachine.createTransition(openState, halfClosedLocal, SENT_DATA_EOS, SENT_HEADERS_EOS); //headers here is trailing headers
		stateMachine.createTransition(openState, closed, SENT_RST, RECV_RST);
		
		stateMachine.createTransition(reservedRemote, halfClosedLocal, RECV_HEADERS);
		stateMachine.createTransition(reservedRemote, closed, RECV_HEADERS_EOS, SENT_RST, RECV_RST);
		
		stateMachine.createTransition(halfClosedRemote, halfClosedRemote, SENT_DATA); //only trailing headers allowed (ie. must have EOS)

		stateMachine.createTransition(halfClosedLocal, halfClosedLocal, RECV_HEADERS, RECV_DATA);

	}

	public CompletableFuture<Void> fireToClient(Stream stream, Http2Response payload) { //, Supplier<StreamTransition> possiblyClose 
		fireToClientImpl(stream, payload);
		return local.fireResponseToApp(stream, payload);
	}
	
	public CompletableFuture<Void> firePushToClient(ClientPushStream stream, Http2Push fullPromise) {
		fireToClientImpl(stream, fullPromise);
		return local.firePushToApp(stream, fullPromise)
				.thenApply( v -> null);
	}

}
