package org.webpieces.router.api.error.dev;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.webpieces.http.StatusCode;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);

	@Before
	public void setUp() {
	}
	
	@Test
	public void testNoMethod() {
//		log.info("starting");
//		String moduleFileContents = NoMethodRouterModules.class.getName();
//		RouterService server = ErrorCommonTest.createServer(false, moduleFileContents);
//
//		//this should definitely not throw since we lazy load everything in dev...
//		server.start();
//		
//		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
//		
//		MockStreamHandle mockStream = new MockStreamHandle();
//		StreamRef ref = server.incomingRequest(req, mockStream);
//		XFuture<StreamWriter> future = ref.getWriter();
//		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally());
//
//		Http2Response response = mockStream.getLastResponse();
//		String body = mockStream.getResponseBody();
//		
//		Assert.assertEquals(StatusCode.HTTP_500_INTERNAL_SVR_ERROR, response.getKnownStatusCode());
//		//Since we have no template installed for converting error routes, body will be ""
//		//Realize that since start did not fail, this test operates differently than the other production ErrorTest.java
//		Assert.assertEquals("", body);
	}

}
