package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.server.ServerStreamWriter;

public class MockRequestListener extends MockSuperclass implements HttpRequestListener {

	private enum Method implements MethodEnum {
		INCOMING
	}
	
	public static class PassedIn {
		public FrontendStream stream;
		public Http2Headers request;
		public Protocol type;
		public PassedIn(FrontendStream stream, Http2Headers headers, Protocol type) {
			super();
			this.stream = stream;
			this.request = headers;
			this.type = type;
		}
	}
	
	@Override
	public ServerStreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		return (ServerStreamWriter) super.calledMethod(Method.INCOMING, new PassedIn(stream, headers, type));
	}

	public void setDefaultRetVal(ServerStreamWriter writer) {
		super.setDefaultReturnValue(Method.INCOMING, writer);
	}
	
	public PassedIn getSingleRequest() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.INCOMING);
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (PassedIn) list.get(0).getArgs()[0];
	}
}
