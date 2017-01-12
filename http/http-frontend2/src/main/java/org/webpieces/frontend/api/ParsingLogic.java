package org.webpieces.frontend.api;

import org.webpieces.httpparser.api.HttpParser;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

public class ParsingLogic {

	private HttpParser httpParser;
	private HpackParser http2Parser;
	private Http2ServerEngineFactory svrEngineFactory;

	public ParsingLogic(HttpParser httpParser, HpackParser http2Parser, Http2ServerEngineFactory svrEngineFactory) {
		this.httpParser = httpParser;
		this.http2Parser = http2Parser;
		this.svrEngineFactory = svrEngineFactory;
	}

	public HttpParser getHttpParser() {
		return httpParser;
	}

	public HpackParser getHttp2Parser() {
		return http2Parser;
	}

	public Http2ServerEngineFactory getSvrEngineFactory() {
		return svrEngineFactory;
	}
	
}
