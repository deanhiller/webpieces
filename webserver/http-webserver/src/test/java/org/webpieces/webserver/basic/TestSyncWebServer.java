package org.webpieces.webserver.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestSyncWebServer {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, null);
		server = webserver.start();		
	}
	
	@Test
	public void testBasic() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
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
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}	

	@Test
	public void testJsonFile() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/somejson");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("red");
		response.assertContentType("application/json");
	}
	
	@Test
	public void testRedirectRawRelativeUrl() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawurlredirect");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}
	
	@Test
	public void testRedirectRawAbsoluteUrl() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawabsoluteurlredirect");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("https://something.com/hi", response.getRedirectUrl());
	}
}
