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
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.routing.app.CorsForAllDomains;
import org.webpieces.webserver.routing.app.CorsForTwoDomains;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class TestOptionsCorsRouting extends AbstractWebpiecesTest {

	private HttpSocket httpsSocket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("domainsMeta2.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), null, false, metaFile);
		webserver.start();
		httpsSocket = connectHttps(false, null, webserver.getUnderlyingHttpsChannel().getLocalAddress());
	}

	@Test
	public void testDomainInvalidResultIn403() {
		String accessHeaders = "Authorization, Content-Type";
		HttpFullRequest req = Requests.createOptionsPreflightRequest("http://notallowed.domain.com", "/content", accessHeaders, "PUT");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertEquals("Origin", varyHeader.getValue());
	}


	@Test
	public void testOptionsDomain1() {
		String accessHeaders = "Authorization, Content-Type";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(CorsForTwoDomains.DOMAIN1, "/content", accessHeaders, "PUT");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_204_NO_CONTENT);
		Header allow = response.getResponse().getHeaderLookupStruct().getHeader("Allow");
		Assert.assertNull(allow); // do not expose methods to a CORS request

		Header cors = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_METHODS);
		Assert.assertEquals("GET, PUT", cors.getValue());

		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_HEADERS);
		Assert.assertEquals("*", header.getValue());

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN1, respDomain.getValue());

		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertNull(varyHeader);

		Header allowCredsHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS);
		Assert.assertEquals("true", allowCredsHeader.getValue());

		String expected = String.join(", ", CorsForTwoDomains.EXPOSED_RESPONSE_HEADERS);
		Header exposeHeaders = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS);
		Assert.assertEquals(expected, exposeHeaders.getValue());
	}

	@Test
	public void testOptionsDomain2() {
		String accessHeaders = "Authorization, Content-Type";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(CorsForTwoDomains.DOMAIN_WITH_PORT, "/content", accessHeaders, "PUT");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_204_NO_CONTENT);
		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN_WITH_PORT, respDomain.getValue());
	}

	@Test
	public void testOptionsDifferentMethods() {
		String accessHeaders = "Authorization, Content-Type";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(CorsForTwoDomains.DOMAIN1, "/content2", accessHeaders, "POST");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_204_NO_CONTENT);
		Header allow = response.getResponse().getHeaderLookupStruct().getHeader("Allow");
		Assert.assertNull(allow); // do not expose methods to a CORS request

		Header cors = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_METHODS);
		Assert.assertEquals("POST, DELETE", cors.getValue());

		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_HEADERS);
		Assert.assertEquals("*", header.getValue());

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN1, respDomain.getValue());
	}




	@Test
	public void testGoodHeadersInAllDomains() {
		String fromDomain = "http://from.this.other.domain.com";
		String accessHeaders = "Authorization";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(fromDomain, "/allDomains", accessHeaders, "POST");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_204_NO_CONTENT);
		Header allow = response.getResponse().getHeaderLookupStruct().getHeader("Allow");
		Assert.assertNull(allow); // do not expose methods to a CORS request

		Header cors = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_METHODS);
		Assert.assertEquals("GET, POST", cors.getValue());

		String expected = String.join(", ", CorsForAllDomains.ALLOWED_REQUEST_HEADERS);
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_HEADERS);
		Assert.assertEquals(expected.toLowerCase(), header.getValue());

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(fromDomain, respDomain.getValue());
		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertEquals("Origin", varyHeader.getValue());

		Header allowCredsHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS);
		Assert.assertNull(allowCredsHeader);

		Header exposeHeaders = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS);
		Assert.assertNull(exposeHeaders);
	}



	@Test
	public void testPostNotInCorsMethodsResult403() {
		String accessHeaders = "Content-Type";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(CorsForTwoDomains.DOMAIN1, "/content", accessHeaders, "POST");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);

		Header cors = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_METHODS);
		Assert.assertEquals("GET, PUT", cors.getValue());

		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_HEADERS);
		Assert.assertEquals("*", header.getValue());

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(CorsForTwoDomains.DOMAIN1, respDomain.getValue());

		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertNull(varyHeader);

		Header allowCredsHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS);
		Assert.assertEquals("true", allowCredsHeader.getValue());

		String expected = String.join(", ", CorsForTwoDomains.EXPOSED_RESPONSE_HEADERS);
		Header exposeHeaders = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS);
		Assert.assertEquals(expected, exposeHeaders.getValue());
	}


	@Test
	public void testBadHeadersResult403() {
		String fromDomain = "http://from.this.domain.com";
		String accessHeaders = "Authorization, Content-Type, NotAllowedHeader";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(fromDomain, "/allDomains", accessHeaders, "POST");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
		Header cors = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_METHODS);
		Assert.assertEquals("GET, POST", cors.getValue());

		String expected = String.join(", ", CorsForAllDomains.ALLOWED_REQUEST_HEADERS);
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_HEADERS);
		Assert.assertEquals(expected.toLowerCase(), header.getValue());

		Header respDomain = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
		Assert.assertEquals(fromDomain, respDomain.getValue());
		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertEquals("Origin", varyHeader.getValue());

		Header allowCredsHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS);
		Assert.assertNull(allowCredsHeader);

		Header exposeHeaders = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS);
		Assert.assertNull(exposeHeaders);
	}

	/**
	 * We may have endpoints but none of these ones support CORS
	 */
	@Test
	public void testOptionsWithZeroMethodsMatchReturns403() {
		String accessHeaders = "Authorization";
		HttpFullRequest req = Requests.createOptionsPreflightRequest(CorsForTwoDomains.DOMAIN_WITH_PORT, "/noMethodsSupportCors", accessHeaders, "GET");

		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_403_FORBIDDEN);
		Header varyHeader = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.VARY);
		Assert.assertNull(varyHeader);
	}
}