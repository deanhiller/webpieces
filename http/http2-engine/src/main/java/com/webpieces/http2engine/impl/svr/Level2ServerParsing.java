package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Level2ServerParsing extends Level2ParsingAndRemoteSettings {

	private Level3SvrIncomingSynchro serverSyncro;

	public Level2ServerParsing(
			Level3SvrIncomingSynchro syncro,
			Level3SvrOutgoingSynchro outSyncro,
			Level7MarshalAndPing notifyListener, 
			HpackParser lowLevelParser, 
			Http2Config config
	) {
		super(syncro, outSyncro, notifyListener, lowLevelParser, config);
		serverSyncro = syncro;
	}
	@Override
	protected CompletableFuture<Void> processSpecific(Http2Msg msg) {
		if(msg instanceof Http2Request) {
			return serverSyncro.processRequest((Http2Request)msg);
		} else
			throw new IllegalArgumentException("Unknown HttpMsg type.  msg="+msg+" type="+msg.getClass());
	}

}
