package org.webpieces.frontend2.impl;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;

public class Http1_1StreamImpl implements FrontendStream {

	private FrontendSocketImpl socket;
	private boolean isActive;

	public Http1_1StreamImpl(FrontendSocketImpl socket, boolean isActive) {
		this.socket = socket;
		this.isActive = isActive;
		
	}
	
	@Override
	public StreamWriter sendResponse(Http2Headers headers) {
		return null;
	}

	@Override
	public StreamWriter sendPush(Http2Push push) {
		throw new UnsupportedOperationException("not supported for http1.1 requests");
	}

	@Override
	public void cancelStream() {
		throw new UnsupportedOperationException("not supported for http1.1 requests.  you can use getSocket().close() instead if you like");
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

}
