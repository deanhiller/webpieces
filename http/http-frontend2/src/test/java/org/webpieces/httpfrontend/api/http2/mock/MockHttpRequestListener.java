package org.webpieces.httpfrontend.api.http2.mock;

import org.webpieces.frontend.api.FrontendStream;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public class MockHttpRequestListener implements HttpRequestListener {

	@Override
	public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		return null;
	}

}
