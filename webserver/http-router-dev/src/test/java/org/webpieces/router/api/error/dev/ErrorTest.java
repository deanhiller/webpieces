package org.webpieces.router.api.error.dev;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.error.ErrorCommonTest;
import org.webpieces.router.api.error.MockStreamHandle;
import org.webpieces.router.api.error.RequestCreation;
import org.webpieces.router.api.mocks.MockResponseStream;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;

import com.webpieces.hpack.api.dto.Http2Request;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);
	private MockResponseStream mockResponseStream = new MockResponseStream();
	private MockStreamHandle nullStream = new MockStreamHandle();

	@Before
	public void setUp() {
	}
	
	@Test
	public void testNoMethod() {
		log.info("starting");
		String moduleFileContents = NoMethodRouterModules.class.getName();
		RouterService server = ErrorCommonTest.createServer(false, moduleFileContents, mockResponseStream);

		//this should definitely not throw since we lazy load everything in dev...
		server.start();
		
		Http2Request req = RequestCreation.createHttpRequest(HttpMethod.GET, "/something");
		
		server.incomingRequest(req, nullStream);

		List<RenderResponse> renders = mockResponseStream.getSendRenderHtmlList();
		Assert.assertEquals(1, renders.size());
		
		RenderResponse renderResponse = renders.get(0);
		Assert.assertEquals(RouteType.INTERNAL_SERVER_ERROR, renderResponse.routeType);
	}

}
