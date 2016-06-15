package org.webpieces.router.api.mocks;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.Response;

public class MockResponseStream extends MockSuperclass implements ResponseStreamer{

	private static enum MockMethod implements MethodEnum {
		SEND_REDIRECT, FAILURE;
	}
	
	@Override
	public void sendRedirect(Response httpResponse) {
		super.calledMethod(MockMethod.SEND_REDIRECT, httpResponse);
	}

	@Override
	public void failure(Throwable e) {
		super.calledMethod(MockMethod.FAILURE, e);
	}
	
	public List<Response> getSendRedirectCalledList() {
		Stream<ParametersPassedIn> params = super.getCalledMethod(MockMethod.SEND_REDIRECT);
		Stream<Response> responseStr = params.map(p -> (Response)p.getArgs()[0]);
		return responseStr.collect(Collectors.toList());
	}

	public Exception getOnlyException() {
		Stream<ParametersPassedIn> calledMethods = super.getCalledMethod(MockMethod.FAILURE);
		Object[] array = calledMethods.toArray();
		Assert.assertEquals(1, array.length);
		
		ParametersPassedIn params = (ParametersPassedIn)array[0];
		
		return (Exception) params.getArgs()[0];
	}

}
