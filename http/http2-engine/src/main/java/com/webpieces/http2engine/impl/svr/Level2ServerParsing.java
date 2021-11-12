package com.webpieces.http2engine.impl.svr;

import org.webpieces.util.futures.XFuture;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;

public class Level2ServerParsing extends Level2ParsingAndRemoteSettings {

	private Level3SvrIncomingSynchro serverSyncro;

	public Level2ServerParsing(
			String key,
			Level3SvrIncomingSynchro syncro,
			Level3SvrOutgoingSynchro outSyncro,
			Level7MarshalAndPing notifyListener, 
			HpackParser lowLevelParser, 
			Http2Config config
	) {
		super(key, syncro, outSyncro, notifyListener, lowLevelParser, config);
		serverSyncro = syncro;
	}
	@Override
	protected XFuture<Void> processSpecific(Http2Msg msg) {
		if(msg instanceof Http2Request) {
			return serverSyncro.processRequest((Http2Request)msg);
		} else if(msg instanceof Http2Push) {
			throw new ConnectionException(CancelReasonCode.PUSH_PROMISE_RECEIVED, msg.getStreamId(), "Server cannot receive PushPromise frames per http/2 spec");
		} else
			throw new IllegalArgumentException("Unknown HttpMsg type.  msg="+msg+" type="+msg.getClass());
	}

}
