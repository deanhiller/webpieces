package com.webpieces.http2engine.api.server;


import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.svr.Level1ServerEngine;

public class Http2ServerEngineFactory {

	public Http2ServerEngine createEngine(Http2Config config, HpackParser http2Parser, ServerEngineListener listener) {
		return new Level1ServerEngine(config, http2Parser, listener);
	}

}
