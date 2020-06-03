package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.util.exceptions.NioClosedChannelException;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.api.server.ServerEngineListener;

public class Layer3Http2EngineListener implements ServerEngineListener {

	//private static final Logger log = LoggerFactory.getLogger(Layer3Http2EngineListener.class);
	
	private FrontendSocketImpl socket;
	private StreamListener httpListener;

	public Layer3Http2EngineListener(FrontendSocketImpl socket, StreamListener httpListener) {
		this.socket = socket;
		this.httpListener = httpListener;
	}

	@Override
	public RequestStreamHandle openStream() {
		HttpStream handle2 = httpListener.openStream(socket);
		return new FrontendStreamProxy(handle2);
	}
	
	public void closeSocket(ShutdownConnection reason) {
		httpListener.fireIsClosed(socket);
		socket.internalClose();
	}
	
	private class FrontendStreamProxy implements RequestStreamHandle {

		private HttpStream handle2;

		public FrontendStreamProxy(HttpStream handle2) {
			this.handle2 = handle2;
		}

		@Override
		public StreamRef process(Http2Request request, ResponseStreamHandle responseListener) {
			Http2StreamImpl stream = new Http2StreamImpl(socket, responseListener, request.getStreamId());
			return handle2.incomingRequest(request, stream);
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

}
