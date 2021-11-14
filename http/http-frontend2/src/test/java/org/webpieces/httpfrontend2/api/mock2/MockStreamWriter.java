package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockStreamWriter extends MockSuperclass implements StreamWriter {

	private enum Method implements MethodEnum {
		SEND_MORE
	}
	
	public void setDefaultRetValToThis() {
		XFuture<Void> completedFuture = XFuture.completedFuture(null);
		super.setDefaultReturnValue(Method.SEND_MORE, completedFuture);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> processPiece(StreamMsg data) {
		return (XFuture<Void>) super.calledMethod(Method.SEND_MORE, data);
	}

	public StreamMsg getSingleFrame() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.SEND_MORE);
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (StreamMsg) list.get(0).getArgs()[0];
	}
	
	public void addProcessResponse(XFuture<Void> future) {
		super.addValueToReturn(Method.SEND_MORE, future);
	}

}
