package org.webpieces.router.api.error;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.*;
import org.webpieces.router.api.error.dev.CommonRoutesModules;
import org.webpieces.router.api.exceptions.InternalErrorRouteFailedException;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.api.simplesvr.NullTemplateApi;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.webpieces.hpack.api.dto.Http2Request;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@RunWith(Parameterized.class)
public class ErrorCommonTest {

	private static final Logger log = LoggerFactory.getLogger(ErrorCommonTest.class);
	private boolean isProdTest;
	private MockResponseStream mockResponseStream;
	private RouterStreamHandle nullStream = new NullStreamHandle();

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		return Arrays.asList(new Object[][] {
	        { true, true , new MockResponseStream()},
	         { false, true, new MockResponseStream() }
	      });
	}
	
	public ErrorCommonTest(boolean isProdTest, boolean expected, MockResponseStream mockResponseStream) {
		this.isProdTest = isProdTest;
		this.mockResponseStream = mockResponseStream;
		log.info("constructing test suite for server prod="+isProdTest);
	}
	
	@Test
	public void testRedirectRouteNotEnoughArguments() {
		//say method is something(int arg, String this)
		//we verify redirects MUST match type and number of method arguments every time
		//then when we form url, we put the stuff in the path OR put it as query params so it works on the way back in again too
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents, mockResponseStream);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/user/5553");

		server.incomingRequest(req, nullStream);
			
		//AFTER the first route fails, it then calls the controller internal error route which fails and
		//then results in ErrorRouteFailedException
		//Of course, usually you are supposed to put a secondary error like ErrorRouteFailedException
		//in the suppressed exceptions of the root exception but internal error routes should never fail
		//so we make it a primary exception to be fixed immediately.
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
		RouterService server = createServer(isProdTest, moduleFileContents, mockResponseStream);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		
		server.incomingRequest(req, nullStream);

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
		RouterService server = createServer(isProdTest, moduleFileContents, mockResponseStream);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/postroute");

		server.incomingRequest(req, nullStream);

		verifyNotFoundRendered(mockResponseStream);
	}
	
	public static RouterService createServer(boolean isProdTest, String moduleFileContents, ResponseStreamer mock) {
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();

		TemplateApi nullApi = new NullTemplateApi();
		if(isProdTest)
			return RouterSvcFactory.create(metrics, f, nullApi, new OverridesForRefactor(mock));
		
		//otherwise create the development server
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		VirtualFile cacheLocation = new VirtualFileImpl(FileFactory.newCacheLocation("webpieces/"+ErrorCommonTest.class.getSimpleName()+"/bytecode"));
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath), cacheLocation);		
		log.info("bytecode dir="+compileConfig.getByteCodeCacheDir());
		RouterService server = DevRouterFactory.create(metrics, f, compileConfig, nullApi, new OverridesForRefactor(mock));
		return server;
	}
}
