package org.webpieces.webserver.routing;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.routing.app.CorsForTwoDomains;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class TestGetCorsRouting extends AbstractWebpiecesTest {

	private HttpSocket httpsSocket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("domainsMeta2.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), null, false, metaFile);
		webserver.start();
		httpsSocket = connectHttps(false, null, webserver.getUnderlyingHttpsChannel().getLocalAddress());
	}

	@Test
	public void testCookie200Ok() {
		String cookieValue = "key1=value1;key2=value2";
		List<String> accessHeaders = List.of("Authorization", "asfsf");
		HttpFullRequest req = Requests.createCorsRequestCookie(CorsForTwoDomains.DOMAIN1, "/content", accessHeaders, KnownHttpMethod.PUT, cookieValue);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
	}

	@Test
	public void testPassingCookieAllHeadersAllowedIs403IfCredsDisallowed() {
		String cookieValue = "key1=value1;key2=value2";
		List<String> accessHeaders = List.of("Authorization", "asfsf");
		HttpFullRequest req = Requests.createCorsRequestCookie(CorsForTwoDomains.DOMAIN1, "/cookie", accessHeaders, KnownHttpMethod.GET, cookieValue);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
	}

	@Test
	public void testNoMatchMethodResultIn404() {
		List<String> accessHeaders = List.of("Authorization");
		HttpFullRequest req = Requests.createCorsRequest("http://notallowed.domain.com", "/noMethodsSupportCors", accessHeaders, KnownHttpMethod.PUT);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}

	@Test
	public void testNoMethodsResultIn403() {
		List<String> accessHeaders = List.of("Authorization");
		HttpFullRequest req = Requests.createCorsRequest("http://notallowed.domain.com", "/noMethodsSupportCors", accessHeaders, KnownHttpMethod.GET);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
	}

	@Test
	public void testDomainInvalidResultIn403() {
		List<String> accessHeaders = List.of("Authorization", "Content-Type");
		HttpFullRequest req = Requests.createCorsRequest("http://notallowed.domain.com", "/content", accessHeaders, KnownHttpMethod.PUT);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
	}

	@Test
	public void testPostNotInCorsMethodsWithPostWithNotCORSRequestAndOriginExists() {
		List<String> accessHeaders = List.of("Authorization");
		HttpFullRequest req = Requests.createNotCorsRequest("/content", accessHeaders, KnownHttpMethod.POST);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
	}

	@Test
	public void testPostNotInCorsMethodsResult403() {
		List<String> accessHeaders = List.of("Authorization");
		HttpFullRequest req = Requests.createCorsRequest(CorsForTwoDomains.DOMAIN1, "/content", accessHeaders, KnownHttpMethod.POST);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
	}

	@Test
	public void testOptionsDomain1() {
		List<String> accessHeaders = List.of("Authorization", "asfsf");
		HttpFullRequest req = Requests.createCorsRequest(CorsForTwoDomains.DOMAIN1, "/content", accessHeaders, KnownHttpMethod.PUT);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);

		Header allow = response.getResponse().getHeaderLookupStruct().getHeader("Allow");
		Assert.assertNull(allow); // do not expose methods to a CORS request

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN1, respDomain.getValue());

		//must tell client since there are two domains, the respond of Origin header may vary(ie. don't cache it)
		Header vary = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertNull(vary);
	}

	@Test
	public void testOptionsDomain2() {
		List<String> accessHeaders = List.of("Authorization", "qwerewr");
		HttpFullRequest req = Requests.createCorsRequest(CorsForTwoDomains.DOMAIN_WITH_PORT, "/content", accessHeaders, KnownHttpMethod.PUT);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN_WITH_PORT, respDomain.getValue());

		//must tell client since there are two domains, the respond of Origin header may vary(ie. don't cache it)
		Header vary = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertNull(vary);
	}

	@Test
	public void testOptionsDifferentMethods() {
		List<String> accessHeaders = List.of("Authorization");
		HttpFullRequest req = Requests.createCorsRequest(CorsForTwoDomains.DOMAIN1, "/content2", accessHeaders, KnownHttpMethod.POST);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN1, respDomain.getValue());
	}

	@Test
	public void testBadHeadersResult403() {
		String fromDomain = "http://from.this.domain.com";
		List<String> accessHeaders = List.of("Authorization", "Content-Type", "NotAllowedHeader");
		HttpFullRequest req = Requests.createCorsRequest(fromDomain, "/allDomains", accessHeaders, KnownHttpMethod.POST);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
	}

	@Test
	public void testGoodHeadersInAllDomains() {
		String fromDomain = "http://from.this.other.domain.com";
		List<String> accessHeaders = List.of("Authorization", "Content-Type");
		HttpFullRequest req = Requests.createCorsRequest(fromDomain, "/allDomains", accessHeaders, KnownHttpMethod.POST);

		XFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(fromDomain, respDomain.getValue());

		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertEquals("Origin", varyHeader.getValue());
	}
}