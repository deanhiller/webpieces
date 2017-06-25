package org.webpieces.webserver.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class TestSyncWebServer extends AbstractWebpiecesTest {

	
	private Http11Socket http11Socket;
	
	@Before
	public void setUp() {
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, null);
		webserver.start();
		http11Socket = http11Simulator.createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());		
	}
	
	@Test
	public void testBasic() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
		response.assertContentType("text/html; charset=utf-8");
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.CONTENT_TYPE);
		Assert.assertEquals(1, headers.size());
	}

	@Test
	public void testAbsoluteHtmlPath() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute2");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
		response.assertContentType("text/html; charset=utf-8");
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.CONTENT_TYPE);
		Assert.assertEquals(1, headers.size());
	}
	
	@Test
	public void testRedirect() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}	

	@Test
	public void testJsonFile() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/somejson");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("red");
		response.assertContentType("application/json");
	}
	
	@Test
	public void testRedirectRawRelativeUrl() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawurlredirect");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}
	
	@Test
	public void testRedirectRawAbsoluteUrl() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawabsoluteurlredirect");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("https://something.com/hi", response.getRedirectUrl());
	}
	
	@Test
	public void testScopedRoot() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/scoped");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}	
	
	@Test
	public void testScopedRootWithSlash() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/scoped/");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}	
}
