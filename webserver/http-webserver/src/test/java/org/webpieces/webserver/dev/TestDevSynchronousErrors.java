package org.webpieces.webserver.dev;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.biz.InternalSvrErrorLib;
import org.webpieces.webserver.basic.biz.NotFoundLib;
import org.webpieces.webserver.mock.MockErrorLib;
import org.webpieces.webserver.mock.MockNotFoundLogic;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * @author dhiller
 *
 */
public class TestDevSynchronousErrors {

	private static final Logger log = LoggerFactory.getLogger(TestDevSynchronousErrors.class);
	private HttpRequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockFrontendSocket that would follow
	//any redirects in the application properly..
	private MockFrontendSocket mockResponseSocket = new MockFrontendSocket();
	private MockNotFoundLogic mockNotFoundLib = new MockNotFoundLogic();
	private MockErrorLib mockInternalSvrErrorLib = new MockErrorLib();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(filePath1+"/src/test/java"));
		
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);

		Module platformOverrides = Modules.combine(
				new DevTemplateModule(templateConfig),
				new ForTestingStaticDevelopmentModeModule());
		
		VirtualFileImpl metaFile = new VirtualFileImpl(filePath1+"/src/test/java/basicMeta.txt");
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WebserverForTest webserver = new WebserverForTest(platformOverrides, new AppOverridesModule(), false, metaFile);
		server = webserver.start();
	}
	
	@Test
	public void testNotFoundRoute() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your app's webpage was not found");
	}
	
	@Test
	public void testNotFoundFromMismatchArgType() {	
		//because 'notAnInt' is not convertable to integer, this result in NotFound rather than 500 as truly a route with
		//no int doesn't really exist so it's a NotFound
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/redirectint/notAnInt");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your app's webpage was not found");		
	}
	
	@Test
	public void testWebappThrowsNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your app's webpage was not found");		
	}
	
	//This would be very weird but make sure it works in case they do it...
	@Test
	public void testInternalSvrErrorRouteThrowsNotFound() {
		mockNotFoundLib.throwRuntime();
		mockInternalSvrErrorLib.throwNotFound();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
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
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
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
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("The webpieces platform saved them");	
	}

	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(NotFoundLib.class).toInstance(mockNotFoundLib);
			binder.bind(InternalSvrErrorLib.class).toInstance(mockInternalSvrErrorLib);
		}
	}
	
}
