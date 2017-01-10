package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpRequestListener;
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

	public HttpServerImpl(HttpRequestListener requestListener, BufferPool bufferPool, FrontendConfig config) {
		HttpParser httpParser = HttpParserFactory.createParser(bufferPool);
		HpackParser http2Parser = HpackParserFactory.createParser(bufferPool, true);

		dataListener = new ServerDataListener(requestListener, httpParser, http2Parser, config);
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
