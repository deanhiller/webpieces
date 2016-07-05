package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;

import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class HttpFrontendImpl implements HttpFrontend {

	private AsyncServer server;
	private DataListenerToParserLayer dataListener;

	public HttpFrontendImpl(TimedListener listener, HttpParser parser, FrontendConfig config, boolean isHttps) {
		ParserLayer nextStage = new ParserLayer(parser, listener, config, isHttps);
		dataListener = new DataListenerToParserLayer(nextStage);
	}
	
	public void init(AsyncServer asyncServer) {
		this.server = asyncServer;
	}
	
	@Override
	public void close() {
		server.closeServerChannel();
	}
	
	public AsyncDataListener getDataListener() {
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
