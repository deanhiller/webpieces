package org.webpieces.httpclient.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2translations.api.Http2ToHttp1_1;
import org.webpieces.httpclient.api.HttpFullRequest;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpparser.api.dto.HttpRequest;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class Http2SocketImpl implements Http2Socket {

	private HttpSocket socket1_1;

	public Http2SocketImpl(HttpSocket socket1_1) {
		this.socket1_1 = socket1_1;
	}

	@Override
	public CompletableFuture<Http2Socket> connect(InetSocketAddress addr) {
		return socket1_1.connect(addr).thenApply(s -> this);
	}

	@Override
	public StreamHandle openStream() {
		return new StreamImpl();
	}
	
	private class StreamImpl implements StreamHandle {
		@Override
		public CompletableFuture<StreamWriter> process(Http2Request request, ResponseHandler responseListener) {
			HttpRequest req = Http2ToHttp1_1.translateRequest(request);
			return socket1_1.send(req, new ResponseListener(socket1_1+"", responseListener))
					.thenApply(s -> new StreamWriterImpl(s, req));
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason payload) {
			throw new UnsupportedOperationException("In http1_1, you can only just close the socket.  call socket.close instead to cancel all requests");
		}
	}
	
	@Override
	public CompletableFuture<FullResponse> send(FullRequest request) {
		HttpFullRequest req = Translations2.translate(request);
		return socket1_1.send(req).thenApply(r -> Translations2.translate(r));
	}

	@Override
	public CompletableFuture<Http2Socket> close() {
		return socket1_1.close().thenApply(s -> this);
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		throw new UnsupportedOperationException("Http1.1 does not support ping");
	}

}
