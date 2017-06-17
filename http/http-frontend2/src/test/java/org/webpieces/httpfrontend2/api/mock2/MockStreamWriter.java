package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class MockStreamWriter extends MockSuperclass implements StreamWriter {

	private enum Method implements MethodEnum {
		SEND_MORE
	}
	
	public void setDefaultRetValToThis() {
		CompletableFuture<Void> completedFuture = CompletableFuture.completedFuture(null);
		super.setDefaultReturnValue(Method.SEND_MORE, completedFuture);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> processPiece(StreamMsg data) {
		return (CompletableFuture<Void>) super.calledMethod(Method.SEND_MORE, data);
	}

	public StreamMsg getSingleFrame() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.SEND_MORE);
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (StreamMsg) list.get(0).getArgs()[0];
	}
	
	public void addProcessResponse(CompletableFuture<Void> future) {
		super.addValueToReturn(Method.SEND_MORE, future);
	}
}
