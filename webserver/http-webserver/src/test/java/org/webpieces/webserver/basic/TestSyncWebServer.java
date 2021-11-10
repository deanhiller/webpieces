package org.webpieces.webserver.basic;

import java.util.List;
import org.webpieces.util.futures.XFuture;
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
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Module;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;


public class TestSyncWebServer extends AbstractWebpiecesTest {

	private HttpSocket http11Socket;
	private SimpleMeterRegistry meterRegistry;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		meterRegistry = new SimpleMeterRegistry();
		
		Module platformOverrides = getOverrides(false, meterRegistry);
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(platformOverrides, null, false, null);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());		
	}
	
	@Test
	public void testBasic() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
		response.assertContentType("text/html; charset=utf-8");
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.CONTENT_TYPE);
		Assert.assertEquals(1, headers.size());
		
		//check metrics are wired correctly here as well
		RequiredSearch result = meterRegistry.get("basicCounter");
		Counter counter = result.counter();
		Assert.assertEquals(1.0, counter.count(), 0.1);
	}

	@Test
	public void testAbsoluteHtmlPath() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute2");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
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
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}	

	@Test
	public void testJsonFile() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/somejson");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("red");
		response.assertContentType("application/json");
	}
	
	@Test
	public void testRedirectRawRelativeUrl() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawurlredirect");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("http://myhost.com/myroute", response.getRedirectUrl());
	}
	
	@Test
	public void testRedirectRawAbsoluteUrl() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/rawabsoluteurlredirect");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
		Assert.assertEquals("https://something.com/hi", response.getRedirectUrl());
	}
	
	@Test
	public void testScopedRoot() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/scoped");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}	
	
	@Test
	public void testScopedRootWithSlash() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/scoped/");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}	
}
