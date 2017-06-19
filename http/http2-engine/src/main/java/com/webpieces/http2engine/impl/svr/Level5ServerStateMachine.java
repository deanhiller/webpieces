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

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.util.locking.PermitQueue;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.impl.shared.Level5CStateMachine;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.StreamException;

public class Level5ServerStateMachine extends Level5CStateMachine {

	private static final Logger log = LoggerFactory.getLogger(Level5ServerStateMachine.class);
	private Level6SvrLocalFlowControl local;
	private HeaderSettings localSettings;
	private HeaderSettings remoteSettings;

	public Level5ServerStateMachine(
			String id, 
			StreamState streamState, 
			Level6RemoteFlowControl remoteFlowControl,
			Level6SvrLocalFlowControl localFlowControl,
			HeaderSettings localSettings,
			HeaderSettings remoteSettings, 
			PermitQueue maxConcurrentQueue
	) {
		super(id, streamState, remoteFlowControl, localFlowControl, maxConcurrentQueue);
		local = localFlowControl;
		this.localSettings = localSettings;
		this.remoteSettings = remoteSettings;
		
		State reservedLocal = stateMachine.createState("Reserved(local)");
	
		NoTransitionImpl failIfNoTransition = new NoTransitionImpl(true);
		NoTransitionImpl streamErrorNoTransition = new NoTransitionImpl(false);
		idleState.setNoTransitionListener(failIfNoTransition);
		openState.setNoTransitionListener(failIfNoTransition);
		closed.setNoTransitionListener(streamErrorNoTransition);
		reservedLocal.setNoTransitionListener(failIfNoTransition);
		halfClosedRemote.setNoTransitionListener(streamErrorNoTransition);
		
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

	public CompletableFuture<Void> fireToClient(ServerStream stream, Http2Request payload) {
		return fireRecvToSM(stream, payload)
				.thenCompose(v-> {
					return local.fireHeadersToClient(stream, payload);
				});
	}

	public CompletableFuture<Void> sendRequestToApp(Http2Request request) {
		if(!streamState.isLargeEnough(request))
			throw new StreamException(CancelReasonCode.CLOSED_STREAM, logId, request.getStreamId(), "Stream id too low and stream not exist(ie. stream was closed) request="+request);
		
		ServerStream stream = createStream(request.getStreamId());
		return fireToClient(stream, request).thenApply(s -> null);
	}
	
	private ServerStream createStream(int streamId) {
		Memento initialState = createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ServerStream stream = new ServerStream(logId, streamId, initialState, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}
	
	private ServerPushStream createPushStream(PushStreamHandleImpl handle, int streamId) {
		Memento initialState = createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ServerPushStream stream = new ServerPushStream(logId, handle, streamId, initialState, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}
	
	@Override
	public CompletableFuture<Void> sendDataToApp(DataFrame frame) {
		return sendDataToAppImpl(frame, false);
	}

	@Override
	protected CompletableFuture<Void> sendTrailersToApp(Http2Trailers frame) {
		return sendTrailersToAppImpl(frame, false);
	}
	
	public CompletableFuture<ServerPushStream> sendPush(PushStreamHandleImpl handle, Http2Push push) {
		int newStreamId = push.getPromisedStreamId();
		ServerPushStream stream = createPushStream(handle, newStreamId);

		return fireToSocket(stream, push)
				.thenApply(s -> stream);
	}

	public CompletableFuture<Void> sendResponseHeaders(Stream stream, Http2Response response) {
		return fireToSocket(stream, response);
	}

}
