package org.webpieces.router.api.simplesvr;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
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
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

@RunWith(Parameterized.class)
public class TestSimpleRoutes {
	
	private static final Logger log = LoggerFactory.getLogger(TestSimpleRoutes.class);
	//protected MockFrontendSocket mockSocket;
	private RoutingService server;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		String moduleFileContents = AppModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		RoutingService prodSvc = RouterSvcFactory.create(f);
		
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		CompileConfig compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath));		
		RoutingService devSvc = DevRouterFactory.create(f, compileConfig);
		
		return Arrays.asList(new Object[][] {
	         { prodSvc, true },
	         { devSvc, true }
	      });
	}

	public TestSimpleRoutes(RoutingService svc, boolean expected) {
		this.server = svc;
		log.info("constructing test suite for server="+svc.getClass().getSimpleName());
	}
	
	@Before
	public void setUp() {
		server.start();
	}

	private class TestModule implements Module {
		@Override
		public void configure(Binder binder) {
		}
	}
	
	@Test
	public void testBasicRoute() {
		Request req = createHttpRequest(HttpMethod.GET, "/something");
		server.processHttpRequests(req);
		
	}

	private Request createHttpRequest(HttpMethod method, String path) {
		Request r = new Request();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}

}
