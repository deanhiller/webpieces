package org.webpieces.httpclient.api.mocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.CancelReason;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;

public class MockResponseListener extends MockSuperclass implements ResponseStreamHandle {

	private enum Method implements MethodEnum {
		PROCESS
	}
	
	@Override
	public StreamRef process(Http2Response response) {
		return (StreamRef) super.calledMethod(Method.PROCESS, response);
	}

	@Override
	public PushStreamHandle openPushStream() {
		return null;
	}

	public Http2Response getIncomingMsg() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.PROCESS);
		if(params.size() != 1)
			throw new IllegalArgumentException("was not called exactly once.  times="+params.size());
		return (Http2Response) params.get(0).getArgs()[0];
	}

	public void addProcessResponse(CompletableFuture<StreamWriter> future) {
		super.addValueToReturn(Method.PROCESS, future);
	}

}
