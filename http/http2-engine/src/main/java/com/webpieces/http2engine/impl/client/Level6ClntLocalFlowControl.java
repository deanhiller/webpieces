package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level6ClntLocalFlowControl extends Level6LocalFlowControl {

	private Level8NotifyClntListeners notify;

	public Level6ClntLocalFlowControl(String logId, Level7MarshalAndPing marshalLayer, Level8NotifyClntListeners notifyListener,
			HeaderSettings localSettings) {
		super(logId, marshalLayer, notifyListener, localSettings);
		notify = notifyListener;
	}

	public CompletableFuture<Void> fireResponseToApp(Stream stream, Http2Response payload) {
		return notify.sendResponseToApp(stream, payload);
	}

	public CompletableFuture<Void> firePushToApp(ClientPushStream stream, Http2Push fullPromise) {
		return notify.sendPushToApp(stream, fullPromise);
	}

}
