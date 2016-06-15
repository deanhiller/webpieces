package org.webpieces.router.api.error;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.error.dev.BadRedirectRouterModules;
import org.webpieces.router.api.error.dev.TooManyArgsRouterModules;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

@RunWith(Parameterized.class)
public class ErrorCommonTest {

	private static final Logger log = LoggerFactory.getLogger(ErrorCommonTest.class);
	private boolean isProdTest;
	
	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		
		return Arrays.asList(new Object[][] {
	         { true, true },
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
		String moduleFileContents = BadRedirectRouterModules.class.getName();
		RoutingService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/user/5553");
		MockResponseStream mockResponseStream = new MockResponseStream();

		server.processHttpRequests(req, mockResponseStream);
			
		Exception e = mockResponseStream.getOnlyException();
		Assert.assertEquals(IllegalArgumentException.class, e.getClass());
		Assert.assertEquals(e.getMessage(), "The Redirect object returned from method='public org.webpieces.router.api.actions.Redirect org.webpieces.devrouter.api.SomeController.badRedirect(int)' has the wrong number of arguments. args.size=0 should be size=1");
	}
	
	@Test
	public void testArgsTypeMismatch() {
		log.info("starting");
		String moduleFileContents = TooManyArgsRouterModules.class.getName();
		RoutingService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		
		server.processHttpRequests(req, mockResponseStream);

		Exception e = mockResponseStream.getOnlyException();
		Assert.assertEquals(NotFoundException.class, e.getClass());
		Assert.assertTrue(e.getMessage().contains("SomeController.argsMismatch(int,java.lang.String)' requires that @Param(id) be of type=int"));
	}
	
	public static RoutingService createServer(boolean isProdTest, String moduleFileContents) {
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		if(isProdTest)
			return RouterSvcFactory.create(f);
		
		//otherwise create the development server
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath));		
		log.info("bytecode dir="+compileConfig.getByteCodeCacheDir());
		RoutingService server = DevRouterFactory.create(f, compileConfig);
		return server;
	}
}
