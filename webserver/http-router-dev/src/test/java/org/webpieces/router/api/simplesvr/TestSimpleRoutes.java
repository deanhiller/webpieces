package org.webpieces.router.api.simplesvr;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.webpieces.util.cmdline2.ArgumentsCheck;
import org.webpieces.util.futures.XFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterServiceFactory;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.error.MockStreamHandle;
import org.webpieces.router.api.error.RequestCreation;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@RunWith(Parameterized.class)
public class TestSimpleRoutes {
	
	private static final Logger log = LoggerFactory.getLogger(TestSimpleRoutes.class);
	private RouterService server;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		String moduleFileContents = AppModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		TestModule module = new TestModule();
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		ArgumentsCheck args = new CommandLineParser().parse();
		RouterConfig config = new RouterConfig(baseWorkingDir, "TestSimpleRoutes")
										.setMetaFile(f)
										.setWebappOverrides(module)
										.setSecretKey(SecretKeyInfo.generateForTest());
		
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		TemplateApi nullApi = new NullTemplateApi();
		RouterService prodSvc = RouterServiceFactory.create(metrics, config, nullApi);
		prodSvc.configure(args);
		args.checkConsumedCorrectly();

		//for dev must be null
		config.setWebappOverrides(null);
		
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		VirtualFile cacheLocation = new VirtualFileImpl(FileFactory.newCacheLocation("webpieces/"+TestSimpleRoutes.class.getSimpleName()+"/bytecode"));
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath), cacheLocation);
		ArgumentsCheck args2 = new CommandLineParser().parse();
		SimpleMeterRegistry metrics2 = new SimpleMeterRegistry();
		RouterService devSvc = DevRouterFactory.create(metrics2, config, compileConfig, nullApi);
		devSvc.configure(args2);
		args2.checkConsumedCorrectly();
		
		return Arrays.asList(new Object[][] {
	         { prodSvc, module },
	         { devSvc, module }
	      });
	}
	
	private static class TestModule implements Module {
		public MockSomeService mockService = new MockSomeService();
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeService.class).toInstance(mockService);
			binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
		}
	}
	
	public TestSimpleRoutes(RouterService svc, TestModule module) {
		this.server = svc;
		log.info("constructing test class for server="+svc.getClass().getSimpleName());
	}
	
	@Before
	public void setUp() {
		server.start();
	}
	
	@Test
	public void testBasicRoute() {
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		
		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		XFuture<StreamWriter> future = ref.getWriter(); 
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
		
		Http2Response resp = mockStream.getLastResponse();
		Assert.assertEquals("http://"+req.getAuthority()+"/something", resp.getSingleHeaderValue(Http2HeaderName.LOCATION));
	}

	@Test
	public void testOneParamRoute() {
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.POST, "/meeting");
		
		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		XFuture<StreamWriter> future = ref.getWriter(); 
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
		
		Http2Response resp = mockStream.getLastResponse();
		Assert.assertEquals("http://"+req.getAuthority()+"/meeting/888", resp.getSingleHeaderValue(Http2HeaderName.LOCATION));
	}
	

}
