package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.impl.shared.Level4PreconditionChecks;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level4ClientPreconditions extends Level4PreconditionChecks<ClientStream> {

	private static final Logger log = LoggerFactory.getLogger(Level4ClientPreconditions.class);
	private Level5ClientStateMachine clientSm;

	public Level4ClientPreconditions(
			Level5ClientStateMachine clientSm
	) {
		super(clientSm);
		this.clientSm = clientSm;
	}

	public CompletableFuture<Stream> createStreamAndSend(Http2Request frame, ResponseHandler responseListener) {
		ConnectionCancelled closedReason = clientSm.getClosedReason();
		if(closedReason != null) {
			return createExcepted(frame, "sending request", closedReason).thenApply((s) -> null);
		}
		
		return clientSm.createStreamAndSend(frame, responseListener);
	}
	
	/**
	 * Return Stream to release IF need to release the stream
	 */
	public CompletableFuture<Void> sendResponseToApp(Http2Response frame) {
		if(clientSm.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		
		return clientSm.sendResponse(frame);
	}
		
	public CompletableFuture<Void> sendPushToApp(Http2Push fullPromise) {		
		if(clientSm.getClosedReason() != null) {
			log.info("ignoring incoming push="+fullPromise+" since socket is shutting down");
			return CompletableFuture.completedFuture(null);
		}
		return clientSm.sendPushToApp(fullPromise);
	}


}
