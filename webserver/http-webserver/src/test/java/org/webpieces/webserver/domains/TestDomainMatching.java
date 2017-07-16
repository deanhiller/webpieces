package org.webpieces.webserver.domains;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;


public class TestDomainMatching extends AbstractWebpiecesTest {

	private HttpSocket httpsSocket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("domainsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(getOverrides(false), null, false, metaFile);
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
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/public/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticFileFromDomain2() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/public/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticDirFromDomain1() {
		HttpFullRequest req = Requests.createGetRequest("mydomain.com", "/public/asyncMeta.txt");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticDirFromDomain2() {
		HttpFullRequest req = Requests.createGetRequest("domain2.com", "/public/asyncMeta.txt");
		
		CompletableFuture<HttpFullResponse> respFuture = httpsSocket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
}
