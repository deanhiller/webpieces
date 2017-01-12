package org.webpieces.httpfrontend2.api;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

class RequestListenerForTest implements HttpRequestListener {

	private boolean isClosed;

	@Override
	public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		return null;
	}

	public boolean isClosed() {
		return false;
	}


}
