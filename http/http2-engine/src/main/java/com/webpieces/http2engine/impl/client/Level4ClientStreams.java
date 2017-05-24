package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.Level4AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.ParseFailReason;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level4ClientStreams extends Level4AbstractStreamMgr<ClientStream> {

	private static final Logger log = LoggerFactory.getLogger(Level4ClientStreams.class);
	private Level5ClientStateMachine clientSm;

	private HeaderSettings localSettings;
	
	private int afterResetExpireSeconds;

	public Level4ClientStreams(
			StreamState state,
			Level5ClientStateMachine clientSm, 
			Level6LocalFlowControl localFlowControl,
			Level6RemoteFlowControl level5FlowControl,
			Http2Config config,
			HeaderSettings remoteSettings
	) {
		super(clientSm, level5FlowControl, localFlowControl, remoteSettings, state);
		this.clientSm = clientSm;
		this.localSettings = config.getLocalSettings();
		this.remoteSettings = remoteSettings;
		afterResetExpireSeconds = config.getAfterResetExpireSeconds();
	}

	public CompletableFuture<Stream> createStreamAndSend(Http2Request frame, ResponseHandler2 responseListener) {
		if(closedReason != null) {
			return createExcepted(frame, "sending request").thenApply((s) -> null);
		}
		
		Stream stream = createStream(frame.getStreamId(), responseListener);
		return clientSm.fireToSocket(stream, frame)
				.thenApply(s -> stream);
	}

	private ClientPushStream createPushStream(int streamId, ResponseHandler2 responseListener) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ClientPushStream stream = new ClientPushStream(streamId, initialState, responseListener, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}
	
	private ClientStream createStream(int streamId, ResponseHandler2 responseListener) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ClientStream stream = new ClientStream(streamId, initialState, responseListener, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}
	
	/**
	 * Return Stream to release IF need to release the stream
	 */
	public CompletableFuture<Void> sendResponseToApp(Http2Response frame) {
		if(closedReason != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		Stream stream = streamState.getStream(frame, true);
		
		return clientSm.fireToClient(stream, frame)
						.thenApply( s -> {
							checkForClosedState(stream, frame, false);
							return null;
						});
//		return clientSm.fireToClient(stream, frame, () -> checkForClosedState(stream, frame, false))
//					.thenApply(t -> {
//						if(t == StreamTransition.STREAM_JUST_CLOSED)
//							return stream;
//						return null;
//					});
	}
		
	public CompletableFuture<Void> sendPushToApp(Http2Push fullPromise) {		
		if(closedReason != null) {
			log.info("ignoring incoming push="+fullPromise+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		int newStreamId = fullPromise.getPromisedStreamId();
		if(newStreamId % 2 == 1)
			throw new ConnectionException(ParseFailReason.INVALID_STREAM_ID, newStreamId, 
					"Server sent bad push promise="+fullPromise+" as new stream id is incorrect and is an odd number");

		ClientStream causalStream = (ClientStream) streamState.getStream(fullPromise, true);
		
		ClientPushStream stream = createPushStream(newStreamId, causalStream.getResponseListener());
		
		return clientSm.firePushToClient(stream, fullPromise);
	}

	@Override
	public CompletableFuture<Void> sendPayloadToApp(PartialStream frame) {
		if(closedReason != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		Stream stream = streamState.getStream(frame, true);
		
		return clientSm.fireToClient(stream, frame)
				.thenApply( s -> {
					checkForClosedState(stream, frame, false);
					return null;
				});
//		return clientSm.fireToClient(stream, frame, () -> checkForClosedState(stream, frame, false))
//					.thenApply( t -> {
//						if(t == StreamTransition.STREAM_JUST_CLOSED)
//							return stream;
//						return null;
//					});
	}


}
