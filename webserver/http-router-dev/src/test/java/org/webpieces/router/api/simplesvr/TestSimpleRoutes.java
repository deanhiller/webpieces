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
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.impl.ctx.FlashImpl;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.ctx.ValidationImpl;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Binder;
import com.google.inject.Module;

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
		RouterConfig config = new RouterConfig()
										.setMetaFile(f)
										.setWebappOverrides(module)
										.setSecretKey(SecretKeyInfo.generateForTest());
		
		RouterService prodSvc = RouterSvcFactory.create(config);

		//for dev must be null
		config.setWebappOverrides(null);
		
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath));		
		RouterService devSvc = DevRouterFactory.create(config, compileConfig);
		
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
		RouterRequest req = createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		Current.setContext(new RequestContext(new ValidationImpl(null), new FlashImpl(null), new SessionImpl(null), req));
		server.incomingCompleteRequest(req, mockResponseStream);
		
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
		Current.setContext(new RequestContext(new ValidationImpl(null), new FlashImpl(null), new SessionImpl(null), req));
		server.incomingCompleteRequest(req, mockResponseStream);
		
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
