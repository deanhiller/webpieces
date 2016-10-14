package org.webpieces.router.api.error.prod;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.error.ErrorCommonTest;
import org.webpieces.router.api.error.RequestCreation;
import org.webpieces.router.api.error.dev.NoMethodRouterModules;
import org.webpieces.router.api.mocks.MockResponseStream;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);
	
	@Test
	public void testNoMethod() {
		log.info("starting");
		String moduleFileContents = NoMethodRouterModules.class.getName();
		RoutingService server = ErrorCommonTest.createServer(true, moduleFileContents);

		try {
			server.start();
			Assert.fail("Should have thrown exception on start since this is prod");
		} catch(RuntimeException e) {
			Assert.assertTrue(e.getMessage().contains("Cannot find 'public' method='thisMethodNotExist' on class="));
		}
		
		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		
		server.incomingCompleteRequest(req, mockResponseStream);
		
		Exception e = mockResponseStream.getOnlyException();
		Assert.assertEquals(IllegalStateException.class, e.getClass());
		Assert.assertTrue(e.getMessage().contains("start was not called by client or start threw"));
	}

}
