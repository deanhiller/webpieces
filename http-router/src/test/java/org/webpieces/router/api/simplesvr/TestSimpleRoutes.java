package org.webpieces.router.api.simplesvr;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestSimpleRoutes {
	
	//protected MockFrontendSocket mockSocket;
	private RoutingService server;
	
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
