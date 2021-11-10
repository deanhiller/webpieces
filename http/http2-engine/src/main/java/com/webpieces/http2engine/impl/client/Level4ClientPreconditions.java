package com.webpieces.http2engine.impl.client;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
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

	public XFuture<Stream> createStreamAndSend(Http2Request frame, ResponseStreamHandle responseListener) {
		ConnectionCancelled closedReason = clientSm.getClosedReason();
		if(closedReason != null) {
			return createExcepted(frame, "sending request", closedReason).thenApply((s) -> null);
		}
		
		return clientSm.createStreamAndSend(frame, responseListener);
	}
	
	/**
	 * Return Stream to release IF need to release the stream
	 */
	public XFuture<Void> sendResponseToApp(Http2Response frame) {
		if(clientSm.getClosedReason() != null) {
			log.info("ignoring incoming frame="+frame+" since socket is shutting down");
			return XFuture.completedFuture(null);
		}
		
		return clientSm.sendResponse(frame);
	}
		
	public XFuture<Void> sendPushToApp(Http2Push fullPromise) {
		if(clientSm.getClosedReason() != null) {
			log.info("ignoring incoming push="+fullPromise+" since socket is shutting down");
			return XFuture.completedFuture(null);
		}
		return clientSm.sendPushToApp(fullPromise);
	}


}
