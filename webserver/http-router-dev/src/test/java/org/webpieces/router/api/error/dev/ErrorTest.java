package org.webpieces.router.api.error.dev;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.error.ErrorCommonTest;
import org.webpieces.router.api.error.RequestCreation;
import org.webpieces.router.api.mocks.MockResponseStream;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);

	@Before
	public void setUp() {
	}
	
	@Test
	public void testNoMethod() {
		log.info("starting");
		String moduleFileContents = NoMethodRouterModules.class.getName();
		RoutingService server = ErrorCommonTest.createServer(false, moduleFileContents);

		//this should definitely not throw since we lazy load everything in dev...
		server.start();
		
		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		
		server.processHttpRequests(req, mockResponseStream);

		//BIT NOTE: The failure MUST come from processHttpReqeusts which proves the compilation/validation
		//happened on processHttpRequests and not during the start() method call like production does it
		//ie. production fails fast while dev keeps recompiling code and only compiles what is going to be
		//run keeping startup times real fast for the dev server(which developers want!!!).  The prod server
		//then starts up slower constructing everything which is ok as webapp users would rather have
		//startup be slow and requesting web pages to be faster
		Exception e = mockResponseStream.getOnlyException();
		Assert.assertEquals(IllegalArgumentException.class, e.getClass());
		Assert.assertTrue(e.getMessage().contains("Cannot find 'public' method="));
	}

}
