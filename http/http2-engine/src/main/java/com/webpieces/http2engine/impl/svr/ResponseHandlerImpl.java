package com.webpieces.http2engine.impl.svr;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.server.ResponseHandler;
import com.webpieces.http2engine.api.server.ServerStreamWriter;
import com.webpieces.http2engine.impl.shared.Stream;

public class ResponseHandlerImpl implements ResponseHandler {

	private Level1ServerEngine level1ServerEngine;
	private Stream stream;

	public ResponseHandlerImpl(Level1ServerEngine level1ServerEngine, Stream stream) {
		this.level1ServerEngine = level1ServerEngine;
		this.stream = stream;
	}

	@Override
	public ServerStreamWriter sendResponse(Http2Headers headerPiece) {
		return null;
	}

	@Override
	public ServerStreamWriter sendPush(Http2Push push) {
		return null;
	}

	@Override
	public void cancelStream() {
	}

}
