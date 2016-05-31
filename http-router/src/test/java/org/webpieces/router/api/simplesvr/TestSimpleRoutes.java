package org.webpieces.router.api.simplesvr;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.file.VirtualFile;
import org.webpieces.router.api.file.VirtualFileInputStream;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestSimpleRoutes {
	
	//protected MockFrontendSocket mockSocket;
	private RoutingService server;
	
	@Before
	public void setUp() {
		String moduleFileContents = AppModules.class.getName();
		
		ByteArrayInputStream in = new ByteArrayInputStream(moduleFileContents.getBytes());
		VirtualFile f = new VirtualFileInputStream(in, "testAppModules");
		
		server = RouterSvcFactory.create(f, new TestModule());
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
