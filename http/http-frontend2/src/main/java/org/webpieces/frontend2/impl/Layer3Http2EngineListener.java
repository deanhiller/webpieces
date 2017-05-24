package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.SocketInfo;

import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2parser.api.dto.error.Http2Exception;

public class Layer3Http2EngineListener implements ServerEngineListener {

	//private static final Logger log = LoggerFactory.getLogger(Layer3Http2EngineListener.class);
	
	private FrontendSocketImpl socket;
	private HttpRequestListener httpListener;
	private SocketInfo socketInfo;

	public Layer3Http2EngineListener(FrontendSocketImpl socket, HttpRequestListener httpListener, SocketInfo socketInfo) {
		this.socket = socket;
		this.httpListener = httpListener;
		this.socketInfo = socketInfo;
	}

	@Override
	public StreamHandle openStream(int streamId, ResponseHandler2 responseHandler) {
		Http2StreamImpl stream = new Http2StreamImpl(socket, responseHandler, streamId);
		return httpListener.openStream(stream, socketInfo);
	}
	
	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer newData) {
		return socket.getChannel().write(newData).thenApply(c -> null);
	}

	public void closeSocket(Http2Exception reason) {
		socket.internalClose();
	}

}
