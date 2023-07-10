package org.webpieces.webserver.test.http2.directfast;

import org.webpieces.frontend2.api.HttpStream;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import org.webpieces.util.context.Context;

import java.util.HashMap;
import java.util.Map;

public class ProxyRequestStreamHandle implements RequestStreamHandle {

	private HttpStream stream;
	private MockFrontendSocket frontendSocket;

	public ProxyRequestStreamHandle(HttpStream stream, MockFrontendSocket frontendSocket) {
		this.stream = stream;
		this.frontendSocket = frontendSocket;
	}

	@Override
	public StreamRef process(Http2Request request, ResponseStreamHandle responseListener) {
		ProxyResponseStream proxyResponse = new ProxyResponseStream(responseListener, frontendSocket);

		Map<String, Object> previous = Context.copyContext();

		//clear context so server uses a clean context
		Context.setContext(new HashMap<>());
		try {
			StreamRef streamRef = stream.incomingRequest(request, proxyResponse);

			return new MockProxyStreamRef(streamRef);
		} finally {
			//client still in this context
			Context.setContext(previous);
		}
	}

}
