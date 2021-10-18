package org.webpieces.webserver.test.http2.directfast;

import org.webpieces.frontend2.api.HttpStream;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import org.webpieces.util.context.Context;

import java.util.Map;

public class ProxyRequestStreamHandle implements RequestStreamHandle {

	private static final String IS_SERVER_SIDE = "_isServerSide";

	private HttpStream stream;
	private MockFrontendSocket frontendSocket;

	public ProxyRequestStreamHandle(HttpStream stream, MockFrontendSocket frontendSocket) {
		this.stream = stream;
		this.frontendSocket = frontendSocket;
	}

	@Override
	public StreamRef process(Http2Request request, ResponseStreamHandle responseListener) {
		ProxyResponseStream proxyResponse = new ProxyResponseStream(responseListener, frontendSocket);

		Boolean isServerSide = (Boolean) Context.get(IS_SERVER_SIDE);

		Map<String, Object> context = Context.copyContext();
		Context.put(IS_SERVER_SIDE, Boolean.TRUE);
		try {
			StreamRef streamRef = stream.incomingRequest(request, proxyResponse);

			return new MockProxyStreamRef(streamRef);
		} finally {
			if(isServerSide == null) {
				//We must simulate being separate from the webserver and the webserver sets and
				//clears the context so we need to capture context and restore it here for tests
				//since everything is single threaded, the server loops around in which case, we
				//do not want to touch the server's context
				Context.restoreContext(context);
			}
		}

	}

}
