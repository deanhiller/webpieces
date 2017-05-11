package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2engine.api.server.ServerStreamWriter;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class MockStreamWriter extends MockSuperclass implements ServerStreamWriter {

	private enum Method implements MethodEnum {
		SEND_MORE
	}
	
	public void setDefaultRetValToThis() {
		CompletableFuture<ServerStreamWriter> completedFuture = CompletableFuture.completedFuture(this);
		super.setDefaultReturnValue(Method.SEND_MORE, completedFuture);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<ServerStreamWriter> sendMore(PartialStream data) {
		return (CompletableFuture<ServerStreamWriter>) super.calledMethod(Method.SEND_MORE, data);
	}

	public PartialStream getSingleFrame() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.SEND_MORE);
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (PartialStream) list.get(0).getArgs()[0];
	}
}
