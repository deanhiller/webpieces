package org.webpieces.router.api.simplesvr;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Binder;
import com.google.inject.Module;

@RunWith(Parameterized.class)
public class TestProdRouter {
	
	private static final Logger log = LoggerFactory.getLogger(TestProdRouter.class);
	private RouterService server;
	private TestModule overrides;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		String moduleFileContents = AppModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		TestModule module = new TestModule();
		Arguments args = new CommandLineParser().parse();
		RouterConfig config = new RouterConfig(baseWorkingDir)
										.setMetaFile(f)
										.setWebappOverrides(module)
										.setSecretKey(SecretKeyInfo.generateForTest());
		RouterService prodSvc = RouterSvcFactory.create(config);
		prodSvc.configure(args);
		args.checkConsumedCorrectly();
		
		return Arrays.asList(new Object[][] {
	         { prodSvc, module }
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
	
	public TestProdRouter(RouterService svc, TestModule module) {
		this.server = svc;
		this.overrides = module;
		log.info("constructing test class for server="+svc.getClass().getSimpleName());
	}
	
	@Before
	public void setUp() {
		server.start();
	}
	
	/**
	 * This test won't work in DevRoute right now as we need to do the following
	 * 1. create CompileOnDemand very early on 
	 * 2. do a Thread.current().setContextClassLoader(compileOnDemand.getLatestClassloader())
	 * 
	 * and this all needs to be done BEFORE TestModule is created and more importantly before
	 * the bind(SomeService.class) as SomeService will be loaded from one classloader and then
	 * when DEVrouter creates the controller, the compileOnDemand classloader is used resulting
	 * in a mismatch.
	 */
	@Test
	public void testAsyncRouteAndMocking() {
		
		RouterRequest req = createHttpRequest(HttpMethod.GET, "/async");

		//setup returning a response
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		overrides.mockService.addToReturn(future);
		
		MockResponseStream mockResponseStream = new MockResponseStream();
		server.incomingCompleteRequest(req, mockResponseStream);
		
		//no response yet...
		List<RedirectResponse> responses = mockResponseStream.getSendRedirectCalledList();
		Assert.assertEquals(0, responses.size());
		
		int id = 78888;
		future.complete(id);

		responses = mockResponseStream.getSendRedirectCalledList();
		Assert.assertEquals(1, responses.size());
		
		RedirectResponse response = responses.get(0);
		Assert.assertEquals(req.domain, response.domain);
		Assert.assertFalse(response.isHttps);
		Assert.assertEquals("/meeting/"+id, response.redirectToPath);		
	}
	
	private RouterRequest createHttpRequest(HttpMethod method, String path) {
		RouterRequest r = new RouterRequest();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}

}
