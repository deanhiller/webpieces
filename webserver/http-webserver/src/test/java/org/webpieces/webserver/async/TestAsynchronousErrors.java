package org.webpieces.webserver.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.biz.InternalSvrErrorLib;
import org.webpieces.webserver.basic.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockErrorLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author dhiller
 *
 */
public class TestAsynchronousErrors {

	private HttpRequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockFrontendSocket that would follow
	//any redirects in the application properly..
	private MockFrontendSocket mockResponseSocket = new MockFrontendSocket();
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();
	private MockErrorLib mockInternalSvrErrorLib = new MockErrorLib();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		VirtualFileClasspath metaFile = new VirtualFileClasspath("asyncMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), new AppOverridesModule(), false, metaFile);
		server = webserver.start();
	}
	
	@Test
	public void testNotFoundRoute() {
		//NOTE: This is adding future to the notFound route 
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());

		//now resolve the future (which would be done on another thread)
		future.complete(22);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your page was not found");
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		CompletableFuture<Integer> future2 = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future2);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		server.processHttpRequests(mockResponseSocket, req, false);

		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());

		future.completeExceptionally(new NotFoundException("some async NotFound"));

		List<FullResponse> responses3 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses3.size());
		
		future2.complete(55);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your page was not found");		
	}
	
	@Test
	public void testNotFoundHandlerThrowsNotFound() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.processHttpRequests(mockResponseSocket, req, false);

		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new NotFoundException("testing notfound from notfound route"));
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");
	}
	
	@Test
	public void testNotFoundThrowsException() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("testing notfound from notfound route"));
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");		
	}
	
	@Test
	public void testNotFoundThrowsThenInternalSvrErrorHandlerThrows() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		CompletableFuture<Integer> future2 = new CompletableFuture<Integer>();
		mockInternalSvrErrorLib.queueFuture(future2);

		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.processHttpRequests(mockResponseSocket, req, false);

		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("fail notfound route"));
		
		List<FullResponse> responses3 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses3.size());
		
		future2.completeExceptionally(new RuntimeException("fail internal server error route"));
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
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.processHttpRequests(mockResponseSocket, req, false);

		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("test async exception"));
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug in our software...sorry about that");	
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		CompletableFuture<Integer> future2 = new CompletableFuture<Integer>();
		mockInternalSvrErrorLib.queueFuture(future2);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("fail notfound route"));
		
		List<FullResponse> responses3 = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses3.size());
		
		future2.completeExceptionally(new RuntimeException("fail internal server error route"));
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("The webpieces platform saved them");	
	}

	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockNotFoundLib);
			binder.bind(InternalSvrErrorLib.class).toInstance(mockInternalSvrErrorLib);
		}
	}
	
}
