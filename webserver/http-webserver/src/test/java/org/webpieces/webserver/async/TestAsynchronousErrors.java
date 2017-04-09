package org.webpieces.webserver.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
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
public class TestAsynchronousErrors {

	private RequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockResponseSender that would follow
	//any redirects in the application properly..
	private MockResponseSender socket = new MockResponseSender();
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();
	private MockSomeLib mockInternalSvrErrorLib = new MockSomeLib();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("asyncMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), new AppOverridesModule(), false, metaFile);
		server = webserver.start();
	}
	
	@Test
	public void testNotFoundRoute() {
		//NOTE: This is adding future to the notFound route 
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());

		//now resolve the future (which would be done on another thread)
		future.complete(22);

		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		CompletableFuture<Integer> future2 = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future2);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		server.incomingRequest(req, new RequestId(0), true, socket);

		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());

		future.completeExceptionally(new NotFoundException("some async NotFound"));

		List<FullResponse> responses3 = socket.getResponses();
		Assert.assertEquals(0, responses3.size());
		
		future2.complete(55);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");		
	}
	
	@Test
	public void testNotFoundHandlerThrowsNotFound() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, new RequestId(0), true, socket);

		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new NotFoundException("testing notfound from notfound route"));
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");
	}
	
	@Test
	public void testNotFoundThrowsException() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("testing notfound from notfound route"));
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");		
	}
	
	@Test
	public void testNotFoundThrowsThenInternalSvrErrorHandlerThrows() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		CompletableFuture<Integer> future2 = new CompletableFuture<Integer>();
		mockInternalSvrErrorLib.queueFuture(future2);

		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.incomingRequest(req, new RequestId(0), true, socket);

		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("fail notfound route"));
		
		List<FullResponse> responses3 = socket.getResponses();
		Assert.assertEquals(0, responses3.size());
		
		future2.completeExceptionally(new RuntimeException("fail internal server error route"));
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("The webpieces platform saved them");
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
		
		server.incomingRequest(req, new RequestId(0), true, socket);

		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("test async exception"));
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");	
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		CompletableFuture<Integer> future2 = new CompletableFuture<Integer>();
		mockInternalSvrErrorLib.queueFuture(future2);
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(0, responses2.size());
		
		future.completeExceptionally(new RuntimeException("fail notfound route"));
		
		List<FullResponse> responses3 = socket.getResponses();
		Assert.assertEquals(0, responses3.size());
		
		future2.completeExceptionally(new RuntimeException("fail internal server error route"));
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("The webpieces platform saved them");	
	}

	@Test
	public void testCompletePromiseAnotherThreadAndPageParamMissing() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future );
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/asyncFailRoute");
		
		server.incomingRequest(req, new RequestId(0), true, socket);

		//now have the server complete processing
		future.complete(5);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software");
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockNotFoundLib);
			binder.bind(SomeLib.class).toInstance(mockInternalSvrErrorLib);
		}
	}
	
}
