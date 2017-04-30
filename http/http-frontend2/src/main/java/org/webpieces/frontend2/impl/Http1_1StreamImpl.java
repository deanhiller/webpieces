package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.server.ServerStreamWriter;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Http1_1StreamImpl implements FrontendStream {

	private FrontendSocketImpl socket;
	private HttpParser http11Parser;

	public Http1_1StreamImpl(FrontendSocketImpl socket, HttpParser http11Parser) {
		this.socket = socket;
		this.http11Parser = http11Parser;
	}
	
	@Override
	public ServerStreamWriter sendResponse(Http2Headers headers) {
		HttpResponse response = Http2Translations.translateResponse(headers);
		
		ByteBuffer buf = http11Parser.marshalToByteBuffer(response);
		socket.write(buf);
		return new StreamImpl();
	}

	private class StreamImpl implements ServerStreamWriter {
		@Override
		public CompletableFuture<ServerStreamWriter> sendMore(PartialStream data) {
			HttpPayload response = Http2Translations.translate(data);
			
			ByteBuffer buf = http11Parser.marshalToByteBuffer(response);
			return socket.write(buf).thenApply(s -> this);
		}
	}
	
	@Override
	public ServerStreamWriter sendPush(Http2Push push) {
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
