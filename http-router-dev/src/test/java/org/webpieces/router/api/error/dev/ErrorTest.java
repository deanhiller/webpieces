package org.webpieces.router.api.error.dev;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
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
		
		Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		MockResponseStream mockResponseStream = new MockResponseStream();
		
		server.processHttpRequests(req, mockResponseStream);

		Exception e = mockResponseStream.getOnlyException();
		Assert.assertEquals(IllegalArgumentException.class, e.getClass());
		Assert.assertTrue(e.getMessage().contains("Cannot find 'public' method="));
	}

}
