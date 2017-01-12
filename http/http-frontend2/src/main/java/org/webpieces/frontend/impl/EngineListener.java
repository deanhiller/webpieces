package org.webpieces.frontend.impl;

import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ResponseHandler;
import com.webpieces.http2engine.api.server.ServerEngineListener;

public class EngineListener implements ServerEngineListener {

	private FrontendSocketImpl socket;
	private HttpRequestListener httpListener;

	public EngineListener(FrontendSocketImpl socket, HttpRequestListener httpListener) {
		this.socket = socket;
		this.httpListener = httpListener;
	}

	@Override
	public StreamWriter receiveRequest(Http2Headers request, ResponseHandler responseHandler) {
		//every request received is a new stream
		FrontendStreamImpl stream = new FrontendStreamImpl(socket, responseHandler);
		return httpListener.incomingRequest(stream, request, Protocol.HTTP2);
	}

	
}
