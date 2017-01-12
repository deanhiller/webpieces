package org.webpieces.frontend2.impl;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ResponseHandler;

public class FrontendStreamImpl implements FrontendStream {

	private FrontendSocketImpl socket;
	private boolean sentResponseHeaders;
	private ResponseHandler responseHandler;

	public FrontendStreamImpl(FrontendSocketImpl socket, ResponseHandler responseHandler) {
		this.socket = socket;
		this.responseHandler = responseHandler;
	}

	@Override
	public StreamWriter sendResponse(Http2Headers headers) {
		sentResponseHeaders = true;
		return responseHandler.sendResponse(headers);
	}

	@Override
	public StreamWriter sendPush(Http2Push push) {
		if(sentResponseHeaders)
			throw new IllegalStateException("You must call sendPush before sendResponse, but after "
					+ "that can send both datastreams back at the same time(see http2 spec for why)");
		return responseHandler.sendPush(push);
	}

	@Override
	public void cancelStream() {
		responseHandler.cancelStream();
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

}
