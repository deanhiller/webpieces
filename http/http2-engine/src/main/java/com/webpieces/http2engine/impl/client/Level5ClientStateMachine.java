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

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.util.locking.PermitQueue;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.Level5CStateMachine;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level5ClientStateMachine extends Level5CStateMachine {

	private Level6ClntLocalFlowControl local;
	private HeaderSettings localSettings;
	private HeaderSettings remoteSettings;
	private int afterResetExpireSeconds;

	public Level5ClientStateMachine(
			String key,
			StreamState streamState,
			Level6RemoteFlowControl remoteFlowControl, 
			Level6ClntLocalFlowControl localFlowControl,
			Http2Config config,
			HeaderSettings remoteSettings, 
			PermitQueue maxConcurrentQueue
	) {
		super(key, streamState, remoteFlowControl, localFlowControl, maxConcurrentQueue);
		this.local = localFlowControl;
		this.localSettings = config.getLocalSettings();
		this.remoteSettings = remoteSettings;
		afterResetExpireSeconds = config.getAfterResetExpireSeconds();
		
		State reservedRemote = stateMachine.createState("Reserved(remote)");
	
		NoTransitionImpl failIfNoTransition = new NoTransitionImpl(true);
		idleState.setNoTransitionListener(failIfNoTransition);
		openState.setNoTransitionListener(failIfNoTransition);
		reservedRemote.setNoTransitionListener(failIfNoTransition);
		halfClosedLocal.setNoTransitionListener(failIfNoTransition);
		closed.setNoTransitionListener(failIfNoTransition);

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

	public CompletableFuture<Void> sendResponse(Http2Response frame) {
		Stream stream = streamState.getStream(frame, true);
		
		CompletableFuture<Void> future = fireToClient(stream, frame);
		return future;
	}
	
	public CompletableFuture<Void> fireToClient(Stream stream, Http2Response payload) { //, Supplier<StreamTransition> possiblyClose 
		return fireRecvToSM(stream, payload)
				.thenCompose(v -> {
					return local.fireResponseToApp(stream, payload);
				});
	}
	
	public CompletableFuture<Void> firePushToClient(ClientPushStream stream, Http2Push fullPromise) {
		return fireRecvToSM(stream, fullPromise)
			.thenCompose(v -> {
				return local.firePushToApp(stream, fullPromise);
			});
	}

	public CompletableFuture<Stream> createStreamAndSend(Http2Request frame, ResponseStreamHandle responseListener) {
		Stream stream = createStream(frame.getStreamId(), responseListener);
		return fireToSocket(stream, frame).thenApply(v -> stream);
	}
	
	private ClientStream createStream(int streamId, ResponseStreamHandle responseListener) {
		Memento initialState = createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ClientStream stream = new ClientStream(logId, streamId, initialState, responseListener, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}

	public CompletableFuture<Void> sendPushToApp(Http2Push fullPromise) {
		int newStreamId = fullPromise.getPromisedStreamId();
		if(newStreamId % 2 == 1)
			throw new ConnectionException(CancelReasonCode.INVALID_STREAM_ID, logId, newStreamId, 
					"Server sent bad push promise="+fullPromise+" as new stream id is incorrect and is an odd number");

		ClientStream causalStream = (ClientStream) streamState.getStream(fullPromise, true);
		
		ClientPushStream stream = createPushStream(newStreamId, causalStream.getResponseListener());
		
		return firePushToClient(stream, fullPromise);
	}

	private ClientPushStream createPushStream(int streamId, ResponseStreamHandle responseListener) {
		Memento initialState = createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ClientPushStream stream = new ClientPushStream(logId, streamId, initialState, responseListener, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}

	public CompletableFuture<Void> sendDataToApp(DataFrame frame) {
		return sendDataToAppImpl(frame, true);
	}

	@Override
	protected CompletableFuture<Void> sendTrailersToApp(Http2Trailers frame) {
		return sendTrailersToAppImpl(frame, true);
	}


}
