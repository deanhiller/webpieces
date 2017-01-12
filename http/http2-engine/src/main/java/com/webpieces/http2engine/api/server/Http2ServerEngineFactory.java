package com.webpieces.http2engine.api.server;


import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.impl.svr.Level1ServerEngine;

public class Http2ServerEngineFactory {

	public Http2ServerEngine createEngine(HpackParser http2Parser, ServerEngineListener listener) {
		return new Level1ServerEngine(http2Parser, listener);
	}

}
