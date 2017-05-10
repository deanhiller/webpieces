package org.webpieces.httpfrontend2.api.mock2;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.server.ServerStreamWriter;

public class MockRequestListener extends MockSuperclass implements HttpRequestListener {

	private enum Method implements MethodEnum {
		INCOMING
	}
	
	@Override
	public ServerStreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		return (ServerStreamWriter) super.calledMethod(Method.INCOMING, stream, headers, type);
	}

	public void setDefaultRetVal(ServerStreamWriter writer) {
		super.setDefaultReturnValue(Method.INCOMING, writer);
	}
}
