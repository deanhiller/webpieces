package org.webpieces.router.api.error;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.error.dev.CommonRoutesModules;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.api.simplesvr.NullTemplateApi;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@RunWith(Parameterized.class)
public class ErrorCommonTest {

	private static final Logger log = LoggerFactory.getLogger(ErrorCommonTest.class);
	private boolean isProdTest;
	private MockResponseStream mockResponseStream;
	private RouterStreamHandle nullStream = new MockStreamHandle();

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

		MockStreamHandle mockStream = new MockStreamHandle();
		CompletableFuture<StreamWriter> future = server.incomingRequest(req, mockStream);
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
		
		
		Http2Response response = mockStream.getLastResponse();
		String contents = mockStream.getResponseBody();

		Assert.assertEquals(response.getSingleHeaderValue(Http2HeaderName.STATUS), "500");
		Assert.assertTrue(contents.contains("This website had a bug, then when rendering the page explaining the bug, well, they hit another bug"));
		
		//We did not send a keep alive so it should close
		Assert.assertTrue(mockStream.isWasClosed());
	}

	@Test
	public void testRedirectRouteNotEnoughArgumentsBUTwithKeepAlive() {
		//say method is something(int arg, String this)
		//we verify redirects MUST match type and number of method arguments every time
		//then when we form url, we put the stuff in the path OR put it as query params so it works on the way back in again too
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents, mockResponseStream);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/user/5553");
		//ADD a keep alive to test keeping alive
		req.addHeader(new Http2Header(Http2HeaderName.CONNECTION, "keep-alive"));

		MockStreamHandle mockStream = new MockStreamHandle();
		CompletableFuture<StreamWriter> future = server.incomingRequest(req, mockStream);
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
		
		Http2Response response = mockStream.getLastResponse();
		String contents = mockStream.getResponseBody();

		Assert.assertEquals(response.getSingleHeaderValue(Http2HeaderName.STATUS), "500");
		Assert.assertTrue(contents.contains("This website had a bug, then when rendering the page explaining the bug, well, they hit another bug"));
		
		//We did not send a keep alive so it should close
		Assert.assertFalse(mockStream.isWasClosed());
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
