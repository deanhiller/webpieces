package org.webpieces.httpfrontend2.api.http2.mock;

import java.util.concurrent.CompletableFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class MockResponseListener extends MockSuperclass implements Http2ResponseListener {

	enum Method implements MethodEnum {
		INCOMING_RESPONSE,
		INCOMING_PUSH,
	}

	private boolean serverCancelled;
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> incomingPartialResponse(PartialStream response) {
		return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_RESPONSE, response);
	}

	@Override
	public PushPromiseListener newIncomingPush(int streamId) {
		return (PushPromiseListener) super.calledMethod(Method.INCOMING_PUSH, streamId);
	}

	@Override
	public void serverCancelledRequest() {
		serverCancelled = true;
	}
	
	public void addReturnValueIncomingResponse(CompletableFuture<Void> future) {
		super.addValueToReturn(Method.INCOMING_RESPONSE, future);
	}
	public void addReturnValuePush(PushPromiseListener retVal) {
		super.addValueToReturn(Method.INCOMING_PUSH, retVal);
	}

	public boolean isServerCancelled() {
		return serverCancelled;
	}

}
