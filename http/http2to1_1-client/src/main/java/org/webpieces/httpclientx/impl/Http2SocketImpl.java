package org.webpieces.httpclientx.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpparser.api.dto.HttpRequest;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

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
	public RequestStreamHandle openStream() {
		return new StreamImpl();
	}
	
	private class StreamImpl implements RequestStreamHandle {
		@Override
		public StreamRef process(Http2Request request, ResponseStreamHandle responseListener) {
			HttpRequest req = Http2ToHttp11.translateRequest(request);
			
			HttpStreamRef streamRef = socket11.send(req, new ResponseListener(socket11+"", responseListener));
			CompletableFuture<StreamWriter> newWriter = streamRef.getWriter().thenApply(s -> new StreamWriterImpl(s, req));
			return new MyStreamRef(newWriter, request);
		}

	}
	
	public class MyStreamRef implements StreamRef {

		private CompletableFuture<StreamWriter> writer;
		private Http2Request request;

		public MyStreamRef(CompletableFuture<StreamWriter> writer, Http2Request request) {
			this.writer = writer;
			this.request = request;
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			return writer;
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reason) {
			String value = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
			if("keep-alive".equals(value)) //do nothing as keep-alive was set so we can't really cancel in http1.1 for this case
				return CompletableFuture.completedFuture(null);
			
			//keep alive not set so close the socket for http1.1
			return socket11.close();
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
