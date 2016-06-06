package org.webpieces.router.api.simplesvr;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

@RunWith(Parameterized.class)
public class TestSimpleRoutes {
	
	//protected MockFrontendSocket mockSocket;
	private RoutingService server;

	@Parameterized.Parameters
	public static Collection bothServers() {
		String moduleFileContents = AppModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		
		RoutingService prodSvc = RouterSvcFactory.create(f);
		RoutingService devSvc = DevRouterFactory.create(f);
		
		return Arrays.asList(new Object[][] {
	         { prodSvc, true },
	         { devSvc, true }
	      });
	}

	public TestSimpleRoutes(RoutingService svc, boolean expected) {
		this.server = svc;
	}
	
	@Before
	public void setUp() {
		String moduleFileContents = AppModules.class.getName();
		
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");
		
		server = RouterSvcFactory.create(f);
		server.start();
	}

	private class TestModule implements Module {
		@Override
		public void configure(Binder binder) {
		}
	}
	
	@Test
	public void testBasicRoute() {
		Request req = createHttpRequest(HttpMethod.GET, "/");
		server.processHttpRequests(req);
		
	}

	private Request createHttpRequest(HttpMethod method, String path) {
		Request r = new Request();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}

}
