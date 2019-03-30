package org.webpieces.router.api.error;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.error.dev.CommonRoutesModules;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.impl.ctx.FlashImpl;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.ctx.ValidationImpl;
import org.webpieces.router.impl.model.bldr.data.InternalErrorRouteFailedException;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@RunWith(Parameterized.class)
public class ErrorCommonTest {

	private static final Logger log = LoggerFactory.getLogger(ErrorCommonTest.class);
	private boolean isProdTest;
	
	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		return Arrays.asList(new Object[][] {
	         { true, true }
	         ,
	         { false, true }
	      });
	}
	
	public ErrorCommonTest(boolean isProdTest, boolean expected) {
		this.isProdTest = isProdTest;
		log.info("constructing test suite for server prod="+isProdTest);
	}
	
	@Test
	public void testRedirectRouteNotEnoughArguments() {
		//say method is something(int arg, String this)
		//we verify redirects MUST match type and number of method arguments every time
		//then when we form url, we put the stuff in the path OR put it as query params so it works on the way back in again too
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.GET, "/user/5553");
		MockResponseStream mockResponseStream = new MockResponseStream();

		Current.setContext(new RequestContext(new ValidationImpl(null), new FlashImpl(null), new SessionImpl(null), req));
		server.incomingCompleteRequest(req, mockResponseStream);
			
		Throwable e = mockResponseStream.getOnlyException();
		Assert.assertEquals(InternalErrorRouteFailedException.class, e.getClass());
		
		while(e.getCause() != null) {
			e = e.getCause();
		}
		
		Assert.assertEquals(IllegalStateException.class, e.getClass());
	}
	
	@Test
	public void testArgsTypeMismatch() {
		log.info("starting");
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		
		Current.setContext(new RequestContext(new ValidationImpl(null), new FlashImpl(null), new SessionImpl(null), req));
		server.incomingCompleteRequest(req, mockResponseStream);

		verifyNotFoundRendered(mockResponseStream);
	}

	private void verifyNotFoundRendered(MockResponseStream mockResponseStream) {
		List<RenderResponse> responses = mockResponseStream.getSendRenderHtmlList();
		Assert.assertEquals(1, responses.size());
		Assert.assertEquals(RouteType.NOT_FOUND, responses.get(0).routeType);
	}
	
	@Test
	public void testGetNotMatchPostRoute() {
		log.info("starting");
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.GET, "/postroute");
		MockResponseStream mockResponseStream = new MockResponseStream();
		
		Current.setContext(new RequestContext(new ValidationImpl(null), new FlashImpl(null), new SessionImpl(null), req));
		server.incomingCompleteRequest(req, mockResponseStream);

		verifyNotFoundRendered(mockResponseStream);
	}
	
	/** 
	 * Need to live test with browser to see if PRG is better or just returning 404 is better!!!
	 * Current behavior is to return a 404
	 */
	//TODO: Test this with browser and then fix for best user experience
//	@Test
//	public void testNotFoundPostRouteResultsInRedirectToNotFoundCatchAllController() {
//		log.info("starting");
//		String moduleFileContents = CommonRoutesModules.class.getName();
//		RoutingService server = createServer(isProdTest, moduleFileContents);
//		
//		server.start();
//		
//		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.POST, "/notexistpostroute");
//		MockResponseStream mockResponseStream = new MockResponseStream();
//		
//		server.incomingCompleteRequest(req, mockResponseStream);
//
//		verifyNotFoundRendered(mockResponseStream);
//	}
	
	public static RouterService createServer(boolean isProdTest, String moduleFileContents) {
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		if(isProdTest)
			return RouterSvcFactory.create(f);
		
		//otherwise create the development server
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath), CompileConfig.getTmpDir());		
		log.info("bytecode dir="+compileConfig.getByteCodeCacheDir());
		RouterService server = DevRouterFactory.create(f, compileConfig);
		return server;
	}
}
