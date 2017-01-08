package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;

public class HttpServerImpl implements HttpServer {

	private AsyncServer server;
	private AsyncDataListener dataListener;

	public HttpServerImpl(TimedRequestListener requestListener, BufferPool bufferPool, FrontendConfig config) {
		HttpParser httpParser = HttpParserFactory.createParser(bufferPool);
		HpackParser http2Parser = HpackParserFactory.createParser(bufferPool, true);

		// The http11layer can be stateless but the http2 engine needs to keep track of
		// state so we need a new http2engine for every connection.. So we can create the http11
		// stuff here but we have to create http2 stuff in the serverdatalistener on
		// every new connection.
		Http11Layer http11Layer = new Http11Layer(httpParser, requestListener, config);
		Http11DataListener http11DataListener = new Http11DataListener(http11Layer);

		dataListener = new ServerDataListener(requestListener, http11DataListener, httpParser, http2Parser, config);
	}
	
	void init(AsyncServer asyncServer) {
		this.server = asyncServer;
	}
	
	@Override
	public CompletableFuture<Void> close() {
		return server.closeServerChannel();
	}
	
	AsyncDataListener getDataListener() {
		return dataListener;
	}

	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
		server.enableOverloadMode(overloadResponse);
	}

	@Override
	public void disableOverloadMode() {
		server.disableOverloadMode();
	}

	@Override
	public TCPServerChannel getUnderlyingChannel() {
		return server.getUnderlyingChannel();
	}

}
