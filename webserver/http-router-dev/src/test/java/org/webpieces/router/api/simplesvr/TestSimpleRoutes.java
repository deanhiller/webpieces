package org.webpieces.router.api.simplesvr;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import org.webpieces.router.api.*;
import org.webpieces.router.api.error.NullStreamHandle;
import org.webpieces.router.api.error.OverridesForRefactor;
import org.webpieces.router.api.error.RequestCreation;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.webpieces.hpack.api.dto.Http2Request;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@RunWith(Parameterized.class)
public class TestSimpleRoutes {
	
	private static final Logger log = LoggerFactory.getLogger(TestSimpleRoutes.class);
	private RouterService server;

	private RouterStreamHandle nullStream = new NullStreamHandle();
	private MockResponseStream mockResponseStream;
	
	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		String moduleFileContents = AppModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		MockResponseStream mock = new MockResponseStream();

		TestModule module = new TestModule();
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		Arguments args = new CommandLineParser().parse();
		RouterConfig config = new RouterConfig(baseWorkingDir)
										.setMetaFile(f)
										.setWebappOverrides(module)
										.setSecretKey(SecretKeyInfo.generateForTest());
		
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		TemplateApi nullApi = new NullTemplateApi();
		RouterService prodSvc = RouterSvcFactory.create(metrics, config, nullApi, new OverridesForRefactor(mock));
		prodSvc.configure(args);
		args.checkConsumedCorrectly();

		//for dev must be null
		config.setWebappOverrides(null);
		
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		VirtualFile cacheLocation = new VirtualFileImpl(FileFactory.newCacheLocation("webpieces/"+TestSimpleRoutes.class.getSimpleName()+"/bytecode"));
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath), cacheLocation);
		Arguments args2 = new CommandLineParser().parse();
		SimpleMeterRegistry metrics2 = new SimpleMeterRegistry();
		RouterService devSvc = DevRouterFactory.create(metrics2, config, compileConfig, nullApi, new OverridesForRefactor(mock));
		devSvc.configure(args2);
		args2.checkConsumedCorrectly();
		
		return Arrays.asList(new Object[][] {
	         { prodSvc, module, mock },
	         { devSvc, module, mock }
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
	
	public TestSimpleRoutes(RouterService svc, TestModule module, MockResponseStream mockResponse) {
		this.server = svc;
		mockResponseStream = mockResponse;
		log.info("constructing test class for server="+svc.getClass().getSimpleName());
	}
	
	@Before
	public void setUp() {
		server.start();
	}
	
	@Test
	public void testBasicRoute() {
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		
		server.incomingRequest(req, nullStream);
		
		List<RedirectResponse> responses = mockResponseStream.getSendRedirectCalledList();
		Assert.assertEquals(1, responses.size());
		
		RedirectResponse response = responses.get(0);
		Assert.assertEquals(req.getAuthority(), response.domain);
		Assert.assertFalse(response.isHttps);
		Assert.assertEquals("/something", response.redirectToPath);
	}

	@Test
	public void testOneParamRoute() {
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.POST, "/meeting");
		server.incomingRequest(req, nullStream);
		
		List<RedirectResponse> responses = mockResponseStream.getSendRedirectCalledList();
		Assert.assertEquals(1, responses.size());
		
		RedirectResponse response = responses.get(0);
		Assert.assertEquals(req.getAuthority(), response.domain);
		Assert.assertFalse(response.isHttps);
		Assert.assertEquals("/meeting/888", response.redirectToPath);
	}
	

}
