package org.webpieces.router.api.error.prod;

import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.error.ErrorCommonTest;
import org.webpieces.router.api.error.MockStreamHandle;
import org.webpieces.router.api.error.RequestCreation;
import org.webpieces.router.api.error.dev.NoMethodRouterModules;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.StatusCode;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);

	@Test
	public void testNoMethod() {
		log.info("starting");
		String moduleFileContents = NoMethodRouterModules.class.getName();
		RouterService server = ErrorCommonTest.createServer(true, moduleFileContents);

		try {
			server.start();
			Assert.fail("Should have thrown exception on start since this is prod");
		} catch(RuntimeException e) {
			Assert.assertTrue(e.getMessage().contains("Cannot find 'public' method='thisMethodNotExist' on class="));
		}
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");

		MockStreamHandle mockStream = new MockStreamHandle();
		StreamRef ref = server.incomingRequest(req, mockStream);
		CompletableFuture<StreamWriter> future = ref.getWriter(); 
		
		//done and no exception SINCE we responded to client successfully
		Assert.assertTrue(future.isDone() && !future.isCompletedExceptionally()); 

		Http2Response response = mockStream.getLastResponse();
		String body = mockStream.getResponseBody();
		
		Assert.assertEquals(StatusCode.HTTP_500_INTERNAL_SVR_ERROR, response.getKnownStatusCode());
		Assert.assertTrue(body.contains("There was a bug in the developers application or webpieces server"));
		
	}

}
