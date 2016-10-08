package org.webpieces.webserver.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestSyncWebServer {

	private MockFrontendSocket socket = new MockFrontendSocket();
	private HttpRequestListener server;
	
	@Before
	public void setUp() {
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, null);
		server = webserver.start();		
	}
	
	@Test
	public void testBasic() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
		response.assertContentType("text/html; charset=utf-8");
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.CONTENT_TYPE);
		Assert.assertEquals(1, headers.size());
	}
	
	@Test
	public void testRedirect() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
	}	

	@Test
	public void testJsonFile() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/somejson");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("red");
		response.assertContentType("application/json");
	}
}
