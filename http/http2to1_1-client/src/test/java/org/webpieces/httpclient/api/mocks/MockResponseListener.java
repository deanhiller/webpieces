package org.webpieces.httpclient.api.mocks;

import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockResponseListener extends MockSuperclass implements ResponseStreamHandle {

	private enum Method implements MethodEnum {
		PROCESS
	}

	private boolean isCancelled;
	
	@SuppressWarnings("unchecked")
	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		return (XFuture<StreamWriter>) super.calledMethod(Method.PROCESS, response);
	}

	@Override
	public PushStreamHandle openPushStream() {
		return null;
	}

	@Override
	public XFuture<Void> cancel(CancelReason payload) {
		isCancelled = true;
		return XFuture.completedFuture(null);
	}

	public Http2Response getIncomingMsg() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.PROCESS);
		if(params.size() != 1)
			throw new IllegalArgumentException("was not called exactly once.  times="+params.size());
		return (Http2Response) params.get(0).getArgs()[0];
	}

	public void addProcessResponse(XFuture<StreamWriter> future) {
		super.addValueToReturn(Method.PROCESS, future);
	}

	public boolean isCancelled() {
		return isCancelled;
	}

}
