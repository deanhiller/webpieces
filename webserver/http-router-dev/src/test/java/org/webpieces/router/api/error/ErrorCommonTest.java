package org.webpieces.router.api.error;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.webpieces.util.futures.XFuture;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterServiceFactory;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.error.dev.CommonRoutesModules;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.api.simplesvr.NullTemplateApi;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

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
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/user/5553");

		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		XFuture<StreamWriter> future = ref.getWriter();
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
		
		
		Http2Response response = mockStream.getLastResponse();
		String contents = mockStream.getResponseBody();

		Assert.assertEquals(response.getSingleHeaderValue(Http2HeaderName.STATUS), "500");
		
		if(isProdTest) {
			//prod template to use
			Assert.assertTrue(contents.contains("There was a bug in the developers application or webpieces server"));
		} else {
			//The internalError template for DevServer should run...
			Assert.assertTrue(contents.contains("NullTemplateApi.java is running for templatePath=org.webpieces.devrouter.impl.internalError_html"));
		}
		
		//We did not send a keep alive so it should close
		Assert.assertTrue(mockStream.isWasClosed());
	}

	@Test
	public void testRedirectRouteNotEnoughArgumentsBUTwithKeepAlive() {
		//say method is something(int arg, String this)
		//we verify redirects MUST match type and number of method arguments every time
		//then when we form url, we put the stuff in the path OR put it as query params so it works on the way back in again too
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/user/5553");
		//ADD a keep alive to test keeping alive
		req.addHeader(new Http2Header(Http2HeaderName.CONNECTION, "keep-alive"));

		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		XFuture<StreamWriter> future = ref.getWriter();
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
		
		Http2Response response = mockStream.getLastResponse();
		String contents = mockStream.getResponseBody();

		Assert.assertEquals(response.getSingleHeaderValue(Http2HeaderName.STATUS), "500");
		
		if(isProdTest) {
			Assert.assertTrue(contents.contains("There was a bug in the developers application or webpieces server."));
			
		} else {
			//use DevServer template
			Assert.assertTrue(contents.contains("NullTemplateApi.java is running for templatePath=org.webpieces.devrouter.impl.internalError_html"));
		}
		
		//We did send a keep alive so it should close
		Assert.assertFalse(mockStream.isWasClosed());
	}
	
	@Test
	public void testArgsTypeMismatch() {
		log.info("starting");
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		
		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		XFuture<StreamWriter> future = ref.getWriter();
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());

		Http2Response response = mockStream.getLastResponse();
		String contents = mockStream.getResponseBody();
		verifyNotFoundRendered(response, contents);

		//We did not send a keep alive so it should close
		Assert.assertTrue(mockStream.isWasClosed());
	}

	private void verifyNotFoundRendered(Http2Response response, String contents) {
		Assert.assertEquals(response.getSingleHeaderValue(Http2HeaderName.STATUS), "404");
		if(isProdTest) //Use production notfouuud page
			Assert.assertEquals(contents, "NullTemplateApi.java is running for templatePath=org.webpieces.devrouter.test.notFound_html"); //no templating engine installed
		else //Use DevServer not foud page
			Assert.assertEquals(contents, "NullTemplateApi.java is running for templatePath=org.webpieces.devrouter.impl.notFound_html"); //no templating engine installed
	}
	
	@Test
	public void testGetNotMatchPostRoute() {
		log.info("starting");
		String moduleFileContents = CommonRoutesModules.class.getName();
		RouterService server = createServer(isProdTest, moduleFileContents);
		
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/postroute");

		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		XFuture<StreamWriter> future = ref.getWriter();
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());

		Http2Response response = mockStream.getLastResponse();
		String contents = mockStream.getResponseBody();
		verifyNotFoundRendered(response, contents);

		//We did not send a keep alive so it should close
		Assert.assertTrue(mockStream.isWasClosed());
	}

	public static RouterService createServer(boolean isProdTest, String moduleFileContents) {
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();

		TemplateApi nullApi = new NullTemplateApi();
		if(isProdTest)
			return RouterServiceFactory.create("ErrorCommonTest", metrics, f, nullApi);
		
		//otherwise create the development server
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		VirtualFile cacheLocation = new VirtualFileImpl(FileFactory.newCacheLocation("webpieces/"+ErrorCommonTest.class.getSimpleName()+"/bytecode"));
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath), cacheLocation);		
		log.info("bytecode dir="+compileConfig.getByteCodeCacheDir());
		RouterService server = DevRouterFactory.create("ErrorCommonTest", metrics, f, compileConfig, nullApi);
		return server;
	}
}
