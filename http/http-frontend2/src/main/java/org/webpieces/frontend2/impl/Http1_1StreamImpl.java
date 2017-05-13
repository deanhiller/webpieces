package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Http1_1StreamImpl implements FrontendStream {

	private FrontendSocketImpl socket;
	private HttpParser http11Parser;
	private AtomicReference<PartialStream> endingFrame = new AtomicReference<>();

	public Http1_1StreamImpl(FrontendSocketImpl socket, HttpParser http11Parser) {
		this.socket = socket;
		this.http11Parser = http11Parser;
	}
	
	@Override
	public CompletableFuture<StreamWriter> sendResponse(Http2Headers headers) {		
		maybeRemove(headers);
		HttpResponse response = Http2Translations.translateResponse(headers);		
		return write(response).thenApply(c -> new StreamImpl());
	}

	private class StreamImpl implements StreamWriter {
		@Override
		public CompletableFuture<StreamWriter> send(PartialStream data) {
			maybeRemove(data);
			List<HttpPayload> responses = Http2Translations.translate(data);
			
			CompletableFuture<Channel> future = CompletableFuture.completedFuture(null);
			for(HttpPayload p : responses) {
				future = future.thenCompose( (s) -> write(p));
			}
			return future.thenApply((s) -> this);
		}
	}
	
	private void maybeRemove(PartialStream data) {
		if(endingFrame.get() != null)
			throw new IllegalStateException("You had already sent a frame with endOfStream "
					+ "set and can't send more.  ending frame was="+endingFrame+" but you just sent="+data);
		
		Http1_1StreamImpl current = socket.getCurrentStream();
		if(current != this)
			throw new IllegalStateException("Due to http1.1 spec, YOU MUST return "
					+ "responses in order and this is not the current response that needs responding to");

		if(!data.isEndOfStream())
			return;
		
		endingFrame.set(data);
		socket.removeStream(this);
	}
	
	private CompletableFuture<Channel> write(HttpPayload payload) {
		ByteBuffer buf = http11Parser.marshalToByteBuffer(payload);
		return socket.getChannel().write(buf);
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
