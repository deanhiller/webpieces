package org.webpieces.webserver.test.http2.directfast;

import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ProxyResponseStream implements ResponseStream {

	private ResponseStreamHandle responseListener;
	private MockFrontendSocket frontendSocket;

	public ProxyResponseStream(ResponseStreamHandle responseListener, MockFrontendSocket frontendSocket) {
		this.responseListener = responseListener;
		this.frontendSocket = frontendSocket;
	}

	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		return responseListener.process(response);
	}

	@Override
	public PushStreamHandle openPushStream() {
		return responseListener.openPushStream();
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		return responseListener.cancel(reason);
	}

	@Override
	public FrontendSocket getSocket() {
		return frontendSocket;
	}

	@Override
	public Map<String, Object> getSession() {
		return null;
	}
	
	

}
