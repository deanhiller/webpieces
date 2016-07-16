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
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

@RunWith(Parameterized.class)
public class TestSimpleRoutes {
	
	private static final Logger log = LoggerFactory.getLogger(TestSimpleRoutes.class);
	private RoutingService server;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		String moduleFileContents = AppModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		TestModule module = new TestModule();
		HttpRouterConfig config = new HttpRouterConfig()
										.setMetaFile(f)
										.setWebappOverrides(module);
		RoutingService prodSvc = RouterSvcFactory.create(config);

		//for dev must be null
		config.setWebappOverrides(null);
		
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath));		
		RoutingService devSvc = DevRouterFactory.create(config, compileConfig);
		
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
		}
	}
	
	public TestSimpleRoutes(RoutingService svc, TestModule module) {
		this.server = svc;
		log.info("constructing test class for server="+svc.getClass().getSimpleName());
	}
	
	@Before
	public void setUp() {
		server.start();
	}
	
	@Test
	public void testBasicRoute() {
		RouterRequest req = createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		server.processHttpRequests(req, mockResponseStream);
		
		List<RedirectResponse> responses = mockResponseStream.getSendRedirectCalledList();
		Assert.assertEquals(1, responses.size());
		
		RedirectResponse response = responses.get(0);
		Assert.assertEquals(req.domain, response.domain);
		Assert.assertFalse(response.isHttps);
		Assert.assertEquals("/something", response.redirectToPath);
	}

	@Test
	public void testOneParamRoute() {
		RouterRequest req = createHttpRequest(HttpMethod.POST, "/meeting");
		MockResponseStream mockResponseStream = new MockResponseStream();
		server.processHttpRequests(req, mockResponseStream);
		
		List<RedirectResponse> responses = mockResponseStream.getSendRedirectCalledList();
		Assert.assertEquals(1, responses.size());
		
		RedirectResponse response = responses.get(0);
		Assert.assertEquals(req.domain, response.domain);
		Assert.assertFalse(response.isHttps);
		Assert.assertEquals("/meeting/888", response.redirectToPath);
	}
	
	private RouterRequest createHttpRequest(HttpMethod method, String path) {
		RouterRequest r = new RouterRequest();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}

}
