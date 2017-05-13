package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Http1_1StreamImpl implements FrontendStream {

	private FrontendSocketImpl socket;
	private HttpParser http11Parser;

	public Http1_1StreamImpl(FrontendSocketImpl socket, HttpParser http11Parser) {
		this.socket = socket;
		this.http11Parser = http11Parser;
	}
	
	@Override
	public CompletableFuture<StreamWriter> sendResponse(Http2Headers headers) {
		HttpResponse response = Http2Translations.translateResponse(headers);
		
		ByteBuffer buf = http11Parser.marshalToByteBuffer(response);
		socket.write(buf);
		return CompletableFuture.completedFuture(new StreamImpl());
	}

	private class StreamImpl implements StreamWriter {
		@Override
		public CompletableFuture<StreamWriter> send(PartialStream data) {
			List<HttpPayload> responses = Http2Translations.translate(data);
			
			CompletableFuture<FrontendSocket> future = CompletableFuture.completedFuture(null);
			for(HttpPayload p : responses) {
				ByteBuffer buf = http11Parser.marshalToByteBuffer(p);
				future = future.thenCompose( (s) ->  
					socket.write(buf)
				);
			}
			return future.thenApply((s) -> this);
		}
	}
	
	@Override
	public CompletableFuture<StreamWriter> sendPush(Http2Push push) {
		throw new UnsupportedOperationException("not supported for http1.1 requests");
	}

	@Override
	public void cancelStream() {
		throw new UnsupportedOperationException("not supported for http1.1 requests.  you can use getSocket().close() instead if you like");
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

}
