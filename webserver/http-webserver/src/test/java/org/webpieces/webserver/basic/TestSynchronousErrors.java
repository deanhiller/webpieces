package org.webpieces.webserver.basic;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * @author dhiller
 *
 */
public class TestSynchronousErrors extends AbstractWebpiecesTest {

	//In the future, we may develop a FrontendSimulator that can be used instead of MockResponseSender that would follow
	//any redirects in the application properly..
	
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();
	private MockSomeLib mockInternalSvrErrorLib = new MockSomeLib();
	private HttpSocket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), new AppOverridesModule(), false, null);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@Test
	public void testNotFoundRoute() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
	}
	
	@Test
	public void testNotFoundFromMismatchArgType() {	
		//because 'notAnInt' is not convertable to integer, this result in NotFound rather than 500 as truly a route with
		//no int doesn't really exist so it's a NotFound
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/redirectint/notAnInt");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");		
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");		
	}
	
	@Test
	public void testNotFoundHandlerThrowsNotFound() {
		mockNotFoundLib.throwNotFound();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");
	}
	
	@Test
	public void testNotFoundThrowsException() {
		mockNotFoundLib.throwRuntime();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");		
	}
	
	@Test
	public void testNotFoundThrowsThenInternalSvrErrorHandlerThrows() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwRuntime();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in the developers application or webpieces server");
	}
	
	//This would be very weird but make sure it works in case they do it...
	@Test
	public void testInternalSvrErrorRouteThrowsNotFound() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwNotFound();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in the developers application or webpieces server");
	}
	
	/**
	 * This tests bug in your webapp "/another" route, you could also test you have a bug in that route AND a bug in your internal
	 * server route as well!!!
	 */
	@Test
	public void testWebAppHasBugRenders500Route() {
		mockNotFoundLib.throwRuntime();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");	
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwRuntime();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in the developers application or webpieces server");	
	}

	//This stack is deeper and a good test to make sure no one breaks the 500 rendering of
	//this by putting some if(responseSent) throw in the wrong place
	@Test
	public void testTemplateHasBug() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/badtemplate");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertContains("There was a bug in our software...sorry about that");	
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);

	}
	
	@Test
	public void testReturnNull() {
		mockNotFoundLib.throwRuntime();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/null");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");		
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockNotFoundLib);
			binder.bind(SomeLib.class).toInstance(mockInternalSvrErrorLib);
		}
	}
	
}
