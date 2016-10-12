package org.webpieces.webserver.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author dhiller
 *
 */
public class TestSynchronousErrors {

	private RequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockResponseSender that would follow
	//any redirects in the application properly..
	private MockResponseSender mockResponseSocket = new MockResponseSender();
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();
	private MockSomeLib mockInternalSvrErrorLib = new MockSomeLib();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		TemplateCompileConfig config = new TemplateCompileConfig();
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), new AppOverridesModule(), false, null);
		server = webserver.start();
	}
	
	@Test
	public void testNotFoundRoute() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your page was not found");
	}
	
	@Test
	public void testNotFoundFromMismatchArgType() {	
		//because 'notAnInt' is not convertable to integer, this result in NotFound rather than 500 as truly a route with
		//no int doesn't really exist so it's a NotFound
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/redirectint/notAnInt");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your page was not found");		
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your page was not found");		
	}
	
	@Test
	public void testNotFoundHandlerThrowsNotFound() {
		mockNotFoundLib.throwNotFound();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");
	}
	
	@Test
	public void testNotFoundThrowsException() {
		mockNotFoundLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");		
	}
	
	@Test
	public void testNotFoundThrowsThenInternalSvrErrorHandlerThrows() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("The webpieces platform saved them");
	}
	
	//This would be very weird but make sure it works in case they do it...
	@Test
	public void testInternalSvrErrorRouteThrowsNotFound() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwNotFound();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("The webpieces platform saved them");
	}
	
	/**
	 * This tests bug in your webapp "/another" route, you could also test you have a bug in that route AND a bug in your internal
	 * server route as well!!!
	 */
	@Test
	public void testWebAppHasBugRenders500Route() {
		mockNotFoundLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");	
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("The webpieces platform saved them");	
	}

	//This stack is deeper and a good test to make sure no one breaks the 500 rendering of
	//this by putting some if(responseSent) throw in the wrong place
	@Test
	public void testTemplateHasBug() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/badtemplate");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertContains("There was a bug in our software...sorry about that");	
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);

	}
	
	@Test
	public void testReturnNull() {
		mockNotFoundLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/null");
		
		server.incomingRequest(req, false, mockResponseSocket);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");		
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockNotFoundLib);
			binder.bind(SomeLib.class).toInstance(mockInternalSvrErrorLib);
		}
	}
	
}
