package org.webpieces.router.api.mocks;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.Response;

public class MockResponseStream extends MockSuperclass implements ResponseStreamer{

	private static enum MockMethod implements MethodEnum {
		SEND_REDIRECT;
	}
	
	@Override
	public void sendRedirect(Response httpResponse) {
		super.calledMethod(MockMethod.SEND_REDIRECT, httpResponse);
	}

	public List<Response> getSendRedirectCalledList() {
		Stream<ParametersPassedIn> params = super.getCalledMethod(MockMethod.SEND_REDIRECT);
		Stream<Response> responseStr = params.map(p -> (Response)p.getArgs()[0]);
		return responseStr.collect(Collectors.toList());
	}

	@Override
	public void failure(Throwable e) {
	}

}
