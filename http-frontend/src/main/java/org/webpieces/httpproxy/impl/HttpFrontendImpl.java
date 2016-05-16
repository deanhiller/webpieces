package org.webpieces.httpproxy.impl;

import java.nio.ByteBuffer;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.httpproxy.api.HttpFrontend;
import org.webpieces.httpproxy.api.HttpRequestListener;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.httpparser.api.HttpParser;

public class HttpFrontendImpl implements HttpFrontend {

	private AsyncServer server;
	private DataListenerToParserLayer dataListener;

	public HttpFrontendImpl(HttpRequestListener listener, HttpParser parser) {
		ParserLayer nextStage = new ParserLayer(parser, listener);
		dataListener = new DataListenerToParserLayer(listener, nextStage);
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
