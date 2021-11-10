package com.webpieces.http2engine.impl.svr;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

public class Level6SvrLocalFlowControl extends Level6LocalFlowControl {

	private Level8NotifySvrListeners notify;

	public Level6SvrLocalFlowControl(String logId, Level7MarshalAndPing marshalLayer, Level8NotifySvrListeners notifyListener,
			HeaderSettings localSettings) {
		super(logId, marshalLayer, notifyListener, localSettings);
		notify = notifyListener;
	}

	public XFuture<Void> fireHeadersToClient(ServerStream stream, Http2Request payload) {
		return notify.fireRequestToApp(stream, payload);
	}

	
	
}
