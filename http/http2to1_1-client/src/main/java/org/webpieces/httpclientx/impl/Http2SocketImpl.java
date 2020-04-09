package org.webpieces.httpclientx.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.HttpRequest;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class Http2SocketImpl implements Http2Socket {

	private HttpSocket socket11;

	public Http2SocketImpl(HttpSocket socket11) {
		this.socket11 = socket11;
	}

	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr) {
		return socket11.connect(addr);
	}

	@Override
	public StreamHandle openStream() {
		return new StreamImpl();
	}
	
	private class StreamImpl implements StreamHandle {
		@Override
		public CompletableFuture<StreamWriter> process(Http2Request request, ResponseHandler responseListener) {
			HttpRequest req = Http2ToHttp11.translateRequest(request);
			return socket11.send(req, new ResponseListener(socket11+"", responseListener))
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
		return socket11.send(req).thenApply(r -> Translations2.translate(r));
	}

	@Override
	public CompletableFuture<Void> close() {
		return socket11.close();
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		throw new UnsupportedOperationException("Http1.1 does not support ping");
	}

}
