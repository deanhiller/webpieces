package org.webpieces.webserver.basic;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;


public class TestSyncWebServer extends AbstractWebpiecesTest {

	
	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		WebserverForTest webserver = new WebserverForTest(getOverrides(false), null, false, null);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());		
	}
	
	@Test
	public void testBasic() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
		response.assertContentType("text/html; charset=utf-8");
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.CONTENT_TYPE);
		Assert.assertEquals(1, headers.size());
	}

	@Test
	public void testAbsoluteHtmlPath() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute2");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
		response.assertContentType("text/html; charset=utf-8");
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.CONTENT_TYPE);
		Assert.assertEquals(1, headers.size());
	}
	
	@Test
	public void testRedirect() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}	

	@Test
	public void testJsonFile() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/somejson");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("red");
		response.assertContentType("application/json");
	}
	
	@Test
	public void testRedirectRawRelativeUrl() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawurlredirect");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}
	
	@Test
	public void testRedirectRawAbsoluteUrl() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawabsoluteurlredirect");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("https://something.com/hi", response.getRedirectUrl());
	}
	
	@Test
	public void testScopedRoot() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/scoped");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}	
	
	@Test
	public void testScopedRootWithSlash() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/scoped/");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}	
}
