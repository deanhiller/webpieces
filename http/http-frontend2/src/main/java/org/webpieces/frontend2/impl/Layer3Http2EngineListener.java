package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2parser.api.dto.CancelReason;

public class Layer3Http2EngineListener implements ServerEngineListener {

	//private static final Logger log = LoggerFactory.getLogger(Layer3Http2EngineListener.class);
	
	private FrontendSocketImpl socket;
	private HttpRequestListener httpListener;

	public Layer3Http2EngineListener(FrontendSocketImpl socket, HttpRequestListener httpListener) {
		this.socket = socket;
		this.httpListener = httpListener;
	}

	@Override
	public StreamHandle openStream() {
		HttpStream handle2 = httpListener.openStream();
		return new FrontendStreamProxy(handle2);
	}
	
	private class FrontendStreamProxy implements StreamHandle {

		private HttpStream handle2;

		public FrontendStreamProxy(HttpStream handle2) {
			this.handle2 = handle2;
		}

		@Override
		public CompletableFuture<StreamWriter> process(Http2Request request, ResponseHandler responseListener) {
			Http2StreamImpl stream = new Http2StreamImpl(socket, responseListener, request.getStreamId());
			return handle2.process(request, stream);
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason payload) {
			return handle2.cancel(payload);
		}
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
