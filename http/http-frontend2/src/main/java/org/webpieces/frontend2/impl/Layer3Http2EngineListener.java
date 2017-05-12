package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ResponseHandler;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.api.server.StreamReference;
import com.webpieces.http2parser.api.Http2Exception;

public class Layer3Http2EngineListener implements ServerEngineListener {

	private FrontendSocketImpl socket;
	private HttpRequestListener httpListener;

	public Layer3Http2EngineListener(FrontendSocketImpl socket, HttpRequestListener httpListener) {
		this.socket = socket;
		this.httpListener = httpListener;
	}

	@Override
	public StreamReference sendRequestToServer(Http2Headers request, ResponseHandler responseHandler) {
		//every request received is a new stream
		Http2StreamImpl stream = new Http2StreamImpl(socket, responseHandler);
		StreamWriter writer = httpListener.incomingRequest(stream, request, Protocol.HTTP2);
		return new StreamRefImpl(stream, httpListener, writer);
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer newData) {
		return socket.getChannel().write(newData).thenApply(c -> null);
	}

	public void closeSocket(Http2Exception reason) {
		socket.internalClose();
	}

}
