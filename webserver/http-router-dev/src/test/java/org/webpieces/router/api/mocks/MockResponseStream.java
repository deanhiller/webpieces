package org.webpieces.router.api.mocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;

public class MockResponseStream extends MockSuperclass implements ResponseStreamer{

	private static enum MockMethod implements MethodEnum {
		SEND_REDIRECT, FAILURE, SEND_RENDER_HTML, SEND_STATIC_HTML;
	}
	
	@Override
	public void sendRedirect(RedirectResponse httpResponse) {
		super.calledVoidMethod(MockMethod.SEND_REDIRECT, httpResponse);
	}

	@Override
	public void sendRenderHtml(RenderResponse resp) {
		super.calledVoidMethod(MockMethod.SEND_RENDER_HTML, resp);
	}
	
	@Override
	public void failureRenderingInternalServerErrorPage(Throwable e) {
		super.calledVoidMethod(MockMethod.FAILURE, e);
	}
	
	public List<RedirectResponse> getSendRedirectCalledList() {
		Stream<ParametersPassedIn> params = super.getCalledMethods(MockMethod.SEND_REDIRECT);
		Stream<RedirectResponse> responseStr = params.map(p -> (RedirectResponse)p.getArgs()[0]);
		return responseStr.collect(Collectors.toList());
	}

	public Exception getOnlyException() {
		Stream<ParametersPassedIn> calledMethods = super.getCalledMethods(MockMethod.FAILURE);
		Object[] array = calledMethods.toArray();
		Assert.assertEquals(1, array.length);
		
		ParametersPassedIn params = (ParametersPassedIn)array[0];
		
		return (Exception) params.getArgs()[0];
	}

	public List<RenderResponse> getSendRenderHtmlList() {
		Stream<ParametersPassedIn> params = super.getCalledMethods(MockMethod.SEND_RENDER_HTML);
		Stream<RenderResponse> responseStr = params.map(p -> (RenderResponse)p.getArgs()[0]);
		return responseStr.collect(Collectors.toList());		
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic) {
		return (CompletableFuture<Void>) super.calledMethod(MockMethod.SEND_STATIC_HTML, renderStatic);
	}



}
