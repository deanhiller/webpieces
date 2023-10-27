package org.webpieces.webserver.https;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;


public class TestHttps extends AbstractWebpiecesTest {
	
	private HttpSocket http11Socket;
	private HttpSocket https11Socket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("httpsMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
		https11Socket = connectHttps(false, null, webserver.getUnderlyingHttpsChannel().getLocalAddress());		
	}

	@Test
	public void testSecureLoginHasHttpsPage() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/internal"); 
		
		XFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		//before we can show you the page, you need to be logged in, redirect to login page...
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
	
	@Test
	public void testBasicPageOverHttps() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secureRoute");
		
		XFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("home page");
	}
	
	@Test
	public void testAccessHttpsPageOverHttp() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secureRoute");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		//Even though the page exists....if accessed over http, it does not exist...
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testSameRouteHttpAndHttpsWrongOrder() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same"); 
		
		XFuture<HttpFullResponse> respFuture1 = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture1);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route"); //notice the Https Route page is not shown		

		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route"); //notice the Https Route page is not shown	
	}

	@Test
	public void testSameRouteHttpAndHttpsCorrectOrder() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same2");
		
		XFuture<HttpFullResponse> respFuture1 = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture1);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Https Route");

		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route");
	}
	
	@Test
	public void testUseHttpButGoThroughLoginFilter() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/randomPage");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		//Even though the page exists....if accessed over http, it does not exist...
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);		
	}
	
	@Test
	public void testSecureLoginNotFoundHttpsPage() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/notFoundPage");
		
		XFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		//Page not exist
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testSecureAndLoggedInAlready() {
		Header cookie = simulateLogin();
		
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/internal"); 
		req.addHeader(cookie);
		
		XFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		//before we can show you the page, you need to be logged in, redirect to login page...
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is some home page");
	}

	@Test
	public void testReverseUrlLookupOnHttpPageForHttpsUrl() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		//since request came in on port 80, url should be port 443
		response.assertContains("https://myhost.com/"); //notice the Https Route page is not shown
	}

	@Test
	public void testReverseUrlLookupOnHttpPageForHttpsUrl8443() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same", 8080);
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("https://myhost.com:8443"); //notice the Https Route page is not shown	
	}
	
	private Header simulateLogin() {
		HttpFullRequest req1 = Requests.createRequest(KnownHttpMethod.POST, "/postLogin");
		
		XFuture<HttpFullResponse> respFuture = https11Socket.send(req1);
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture);
		Header header = response1.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
		String value = header.getValue();
		value = value.replace("; path=/; HttpOnly", "");
		Header cookie = new Header(KnownHeaderName.COOKIE, value);
		
		return cookie;
	}
}
