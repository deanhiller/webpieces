package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;

import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.api.server.ServerEngineListener;

public class Layer3Http2EngineListener implements ServerEngineListener {

	//private static final Logger log = LoggerFactory.getLogger(Layer3Http2EngineListener.class);
	
	private FrontendSocketImpl socket;
	private HttpRequestListener httpListener;

	public Layer3Http2EngineListener(FrontendSocketImpl socket, HttpRequestListener httpListener) {
		this.socket = socket;
		this.httpListener = httpListener;
	}

	@Override
	public StreamHandle openStream(int streamId, ResponseHandler responseHandler) {
		Http2StreamImpl stream = new Http2StreamImpl(socket, responseHandler, streamId);
		return httpListener.openStream(stream);
	}
	
	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer newData) {
		try {
			return socket.getChannel().write(newData).thenApply(c -> null);
		} catch(NioClosedChannelException e) {
			CompletableFuture<Void> f = new CompletableFuture<Void>();
			f.completeExceptionally(e);
			return f;
		}
	}

	public void closeSocket(ShutdownConnection reason) {
		socket.internalClose();
	}

}
