package org.webpieces.webserver.domains;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class TestDomainMatching extends AbstractWebpiecesTest {

	private HttpSocket httpsSocket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("domainsMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false), null, false, metaFile);
		webserver.start();
		httpsSocket = connectHttps(false, null, webserver.getUnderlyingHttpsChannel().getLocalAddress());
	}

	@Test
	public void testDomain1RequestDomain1Route() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/domain1");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is domain1");
	}

	@Test
	public void testDomain1RequestDomain1RouteWithPort() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com:9000", "/domain1");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is domain1");
	}
	
	@Test
	public void testDomain1RequestDomain2Route() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/domain2");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page for Domain1 was not found");
	}
	
	@Test
	public void testDomain2RequestDomain1Route() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/domain1");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
	}
	
	@Test
	public void testDomain2RequestDomain2Route() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/domain2");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is domain2");
	}

	@Test
	public void testStaticFileFromDomain1() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/public1/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticFileFromDomain2NotFoundInDomain1() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/public2/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testStaticFileFromDomain2() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/public2/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticFileFromDomain1NotFoundInDomain2() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/public1/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testStaticDirFromDomain1() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/public1/asyncMeta.txt");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticDirFromDomain2NotFoundInDomain1() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/public2/asyncMeta.txt");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testStaticDirFromDomain2() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/public2/asyncMeta.txt");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
}
