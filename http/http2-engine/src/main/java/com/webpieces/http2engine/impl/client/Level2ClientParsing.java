package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;

public class Level2ClientParsing extends Level2ParsingAndRemoteSettings {

	private Level3ClntIncomingSynchro clientSyncro;

	public Level2ClientParsing(
			String key,
			Level3ClntIncomingSynchro syncro,
			Level3ClntOutgoingSyncro outSyncro,
			Level7MarshalAndPing notifyListener, 
			HpackParser lowLevelParser, 
			Http2Config config
	) {
		super(key, syncro, outSyncro, notifyListener, lowLevelParser, config);
		clientSyncro = syncro;
	}
	
	@Override
	protected CompletableFuture<Void> processSpecific(Http2Msg msg) {
		if(msg instanceof Http2Response) {
			return clientSyncro.sendResponseToApp((Http2Response) msg);
		} else if(msg instanceof Http2Push) {
			return clientSyncro.sendPushToApp((Http2Push) msg);			
		} else
			throw new IllegalArgumentException("Unknown HttpMsg type.  msg="+msg+" type="+msg.getClass());
	}

}
