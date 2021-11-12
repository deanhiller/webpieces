package com.webpieces.http2engine.impl.svr;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.impl.shared.Level4PreconditionChecks;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level4ServerPreconditions extends Level4PreconditionChecks<ServerStream> {

	private final static Logger log = LoggerFactory.getLogger(Level4ServerPreconditions.class);

	private Level5ServerStateMachine serverSm;

	private String logId;
	
	public Level4ServerPreconditions(String logId, Level5ServerStateMachine serverSm) {
		super(serverSm);
		this.logId = logId;
		this.serverSm = serverSm;
	}

	public XFuture<Void> sendRequestToApp(Http2Request request) {
		if(request.getStreamId() % 2 == 0)
			throw new ConnectionException(CancelReasonCode.BAD_STREAM_ID, logId, request.getStreamId(), "Bad stream id.  Even stream ids not allowed in requests to a server request="+request);

		return serverSm.sendRequestToApp(request);
	}
	
	public XFuture<ServerPushStream> sendPush(PushStreamHandleImpl handle, Http2Push push) {
		return serverSm.sendPush(handle, push);
	}

	public XFuture<Void> sendResponseToSocket(Stream stream, Http2Response response) {
		ConnectionCancelled closedReason = serverSm.getClosedReason();
		if(closedReason != null) {
			return createExcepted(response, "sending response", closedReason);
		}

		return serverSm.sendResponseHeaders(stream, response);
	}



}
