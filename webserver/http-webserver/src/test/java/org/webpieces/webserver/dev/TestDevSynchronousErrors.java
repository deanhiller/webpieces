package org.webpieces.webserver.dev;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Modules;

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
	private Http11Socket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(filePath1+"/src/test/java"));
		
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(false);

		Module platformOverrides = Modules.combine(
				new PlatformOverridesForTest(mgr, time, mockTimer, templateConfig),
				new ForTestingStaticDevelopmentModeModule());
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WebserverForTest webserver = new WebserverForTest(platformOverrides, new AppOverridesModule(), false, null);
		webserver.start();
		http11Socket = http11Simulator.createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@Test
	public void testNotFoundRoute() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");
	}
	
	@Test
	public void testNotFoundFromMismatchArgType() {	
		//because 'notAnInt' is not convertable to integer, this result in NotFound rather than 500 as truly a route with
		//no int doesn't really exist so it's a NotFound
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/redirectint/notAnInt");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");		
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");		
	}
	
	//This would be very weird but make sure it works in case they do it...
	@Test
	public void testInternalSvrErrorRouteThrowsNotFound() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwNotFound();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		http11Socket.send(req);

		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("The webpieces platform saved them");
	}
	
	/**
	 * This tests bug in your webapp "/another" route, you could also test you have a bug in that route AND a bug in your internal
	 * server route as well!!!
	 */
	@Test
	public void testWebAppHasBugRenders500Route() {
		mockNotFoundLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("There was a bug in our software...sorry about that");	
	}
	
	@Test
	public void testWebAppHasBugAndRender500HasBug() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwRuntime();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("The webpieces platform saved them");	
	}

//	@Test
//	public void testNotFoundJsonInDevMode() {
//		mockNotFoundLib.throwRuntime();
//		mockInternalSvrErrorLib.throwRuntime();
//		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/json/notfound");
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
