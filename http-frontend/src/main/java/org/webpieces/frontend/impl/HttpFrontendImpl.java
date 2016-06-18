package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.handlers.DataListener;

public class HttpFrontendImpl implements HttpFrontend {

	private AsyncServer server;
	private DataListenerToParserLayer dataListener;

	public HttpFrontendImpl(HttpRequestListener listener, HttpParser parser, boolean isHttps) {
		ParserLayer nextStage = new ParserLayer(parser, listener, isHttps);
		dataListener = new DataListenerToParserLayer(nextStage);
	}
	
	public void init(AsyncServer asyncServer) {
		this.server = asyncServer;
	}
	
	@Override
	public void close() {
		server.closeServerChannel();
	}
	
	public DataListener getDataListener() {
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
}
