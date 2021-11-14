package org.webpieces.webserver.async;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.util.file.VirtualFileClasspath;
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
public class TestAsynchronousErrors extends AbstractWebpiecesTest {

	//In the future, we may develop a FrontendSimulator that can be used instead of MockResponseSender that would follow
	//any redirects in the application properly..
	
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();
	private MockSomeLib mockInternalSvrErrorLib = new MockSomeLib();
	private HttpSocket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("asyncMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), new AppOverridesModule(), false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@Test
	public void testNotFoundRoute() {
		//NOTE: This is adding future to the notFound route 
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());

		//now resolve the future (which would be done on another thread)
		future.complete(22);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		XFuture<Integer> future2 = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future2);
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		Assert.assertFalse(respFuture.isDone());

		future.completeExceptionally(new NotFoundException("some async NotFound"));

		Assert.assertFalse(respFuture.isDone());
		
		future2.complete(55);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");		
	}
	
	@Test
	public void testNotFoundHandlerThrowsNotFound() {
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		Assert.assertFalse(respFuture.isDone());
		
		future.completeExceptionally(new NotFoundException("testing notfound from notfound route"));
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");
	}
	
	@Test
	public void testNotFoundThrowsException() {
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());
		
		future.completeExceptionally(new RuntimeException("testing notfound from notfound route"));
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");		
	}
	
	@Test
	public void testNotFoundThrowsThenInternalSvrErrorHandlerThrows() {
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		XFuture<Integer> future2 = new XFuture<Integer>();
		mockInternalSvrErrorLib.queueFuture(future2);

		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		Assert.assertFalse(respFuture.isDone());
		
		future.completeExceptionally(new RuntimeException("fail notfound route"));
		
		Assert.assertFalse(respFuture.isDone());
		
		future2.completeExceptionally(new RuntimeException("fail internal server error route"));
		
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
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		Assert.assertFalse(respFuture.isDone());
		
		future.completeExceptionally(new RuntimeException("test async exception"));
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");	
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future);
		XFuture<Integer> future2 = new XFuture<Integer>();
		mockInternalSvrErrorLib.queueFuture(future2);
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());
		
		future.completeExceptionally(new RuntimeException("fail notfound route"));
		
		Assert.assertFalse(respFuture.isDone());
		
		future2.completeExceptionally(new RuntimeException("fail internal server error route"));
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in the developers application or webpieces server");	
	}

	@Test
	public void testCompletePromiseAnotherThreadAndPageParamMissing() {
		XFuture<Integer> future = new XFuture<Integer>();
		mockNotFoundLib.queueFuture(future );
		
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/asyncFailRoute");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		//now have the server complete processing
		future.complete(5);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
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
