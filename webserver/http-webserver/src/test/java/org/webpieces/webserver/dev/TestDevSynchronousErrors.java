package org.webpieces.webserver.dev;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileFactory;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.OverridesForEmbeddedSvrWithParsing;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * @author dhiller
 *
 */
public class TestDevSynchronousErrors extends AbstractWebpiecesTest {

	private static final Logger log = LoggerFactory.getLogger(TestDevSynchronousErrors.class);
	
	//In the future, we may develop a FrontendSimulator that can be used instead of MockResponseSender that would follow
	//any redirects in the application properly..
	
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();
	private MockSomeLib mockInternalSvrErrorLib = new MockSomeLib();
	private HttpSocket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(VirtualFileFactory.newBaseFile("src/test/java"));
		
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(false);

		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		Module platformOverrides = Modules.combine(
				new OverridesForEmbeddedSvrWithParsing(mgr, time, mockTimer, templateConfig, metrics),
				new ForTestingStaticDevelopmentModeModule());
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(platformOverrides, new AppOverridesModule(), false, null);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@Test
	public void testNotFoundRoute() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");
	}
	
	@Test
	public void testNotFoundFromMismatchArgType() {	
		//because 'notAnInt' is not convertable to integer, this result in NotFound rather than 500 as truly a route with
		//no int doesn't really exist so it's a NotFound
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/redirectint/notAnInt");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");		
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");		
	}
	
	//This would be very weird but make sure it works in case they do it...
	@Test
	public void testInternalSvrErrorRouteThrowsNotFound() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwNotFound();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture);
		response1.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response1.assertContains("/?webpiecesShowInternalErrorPage=true"); //There should be a callback url to render what shows in production
		
		//callback shows the original page...
		HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/?webpiecesShowInternalErrorPage=true");
		XFuture<HttpFullResponse> respFuture2 = http11Socket.send(req2);
		ResponseWrapper response2 = ResponseExtract.waitResponseAndWrap(respFuture2);
		response2.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response2.assertContains("There was a bug in the developers application or webpieces server");
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
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture);
		response1.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response1.assertContains("/?webpiecesShowInternalErrorPage=true"); //There should be a callback url to render what shows in production
		
		//callback shows the original page...
		HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/?webpiecesShowInternalErrorPage=true");
		XFuture<HttpFullResponse> respFuture2 = http11Socket.send(req2);
		ResponseWrapper response2 = ResponseExtract.waitResponseAndWrap(respFuture2);
		response2.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response2.assertContains("There was a bug in our software...sorry about that");
		
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwRuntime();
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture);
		response1.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response1.assertContains("/?webpiecesShowInternalErrorPage=true"); //There should be a callback url to render what shows in production
		
		//callback shows the original page...
		HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/?webpiecesShowInternalErrorPage=true");
		XFuture<HttpFullResponse> respFuture2 = http11Socket.send(req2);
		ResponseWrapper response2 = ResponseExtract.waitResponseAndWrap(respFuture2);
		response2.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response2.assertContains("There was a bug in the developers application or webpieces server");
	}

//	@Test
//	public void testNotFoundJsonInDevMode() {
//		mockNotFoundLib.throwRuntime();
//		mockInternalSvrErrorLib.throwRuntime();
//		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/json/notfound");
//		
//		server.incomingRequest(req, new RequestId(0), true, socket);
//		
//		FullResponse response = ResponseExtract.assertSingleResponse(socket);
//		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
//		//perhaps we should just show json here.....
//		response.assertContains("You are in the WebPieces Development Server");	
//	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockNotFoundLib);
			binder.bind(SomeLib.class).toInstance(mockInternalSvrErrorLib);
		}
	}
	
}
