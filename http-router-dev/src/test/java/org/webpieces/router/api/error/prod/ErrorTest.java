package org.webpieces.router.api.error.prod;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.error.dev.NoMethodRouterModules;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);
	private RoutingService server;

	@Test
	public void testNoMethod() {
		log.info("starting");
		String moduleFileContents = NoMethodRouterModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		server = RouterSvcFactory.create(f);
		Request req = createHttpRequest(HttpMethod.GET, "/something");

		try {
			server.start();
			Assert.fail("Should have thrown exception on start since this is prod");
		} catch(IllegalArgumentException e) {
		}
		
		try {
			server.processHttpRequests(req);
			Assert.fail("should have thrown");
		} catch(IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("start was not called by client or start threw"));
		}
		
	}

	private Request createHttpRequest(HttpMethod method, String path) {
		Request r = new Request();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}

}
