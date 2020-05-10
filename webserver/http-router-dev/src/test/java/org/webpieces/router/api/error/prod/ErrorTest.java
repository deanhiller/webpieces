package org.webpieces.router.api.error.prod;

import com.webpieces.http2engine.api.StreamWriter;
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
import org.webpieces.router.api.mocks.MockResponseStream;

import com.webpieces.hpack.api.dto.Http2Request;

import java.util.concurrent.CompletableFuture;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);
	private MockResponseStream mockResponseStream = new MockResponseStream();
	private MockStreamHandle nullStream = new MockStreamHandle();

	@Test
	public void testNoMethod() {
		log.info("starting");
		String moduleFileContents = NoMethodRouterModules.class.getName();
		RouterService server = ErrorCommonTest.createServer(true, moduleFileContents, mockResponseStream);

		try {
			server.start();
			Assert.fail("Should have thrown exception on start since this is prod");
		} catch(RuntimeException e) {
			Assert.assertTrue(e.getMessage().contains("Cannot find 'public' method='thisMethodNotExist' on class="));
		}
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");

		CompletableFuture<StreamWriter> future = server.incomingRequest(req, nullStream);
		Assert.assertTrue(future.isCompletedExceptionally());

		//TODO(dhiller): make sure we sent response to customers
//		Exception e = mockResponseStream.getOnlyException();
//		Assert.assertEquals(IllegalStateException.class, e.getClass());
//		Assert.assertTrue(e.getMessage().contains("start was not called by client or start threw"));
	}

}
