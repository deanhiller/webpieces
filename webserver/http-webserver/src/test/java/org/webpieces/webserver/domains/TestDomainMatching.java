package org.webpieces.webserver.domains;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class TestDomainMatching extends AbstractWebpiecesTest {

	private Http11Socket httpsSocket;

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("domainsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		httpsSocket = http11Simulator.openHttps();
	}

	@Test
	public void testDomain1RequestDomain1Route() {
		HttpRequest req = Requests.createGetRequest("mydomain.com", "/domain1");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is domain1");
	}

	@Test
	public void testDomain1RequestDomain1RouteWithPort() {
		HttpRequest req = Requests.createGetRequest("mydomain.com:9000", "/domain1");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is domain1");
	}
	
	@Test
	public void testDomain1RequestDomain2Route() {
		HttpRequest req = Requests.createGetRequest("mydomain.com", "/domain2");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page for Domain1 was not found");
	}
	
	@Test
	public void testDomain2RequestDomain1Route() {
		HttpRequest req = Requests.createGetRequest("domain2.com", "/domain1");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
	}
	
	@Test
	public void testDomain2RequestDomain2Route() {
		HttpRequest req = Requests.createGetRequest("domain2.com", "/domain2");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is domain2");
	}

	@Test
	public void testStaticFileFromDomain1() {
		HttpRequest req = Requests.createGetRequest("mydomain.com", "/public/myfile");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticFileFromDomain2() {
		HttpRequest req = Requests.createGetRequest("domain2.com", "/public/myfile");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticDirFromDomain1() {
		HttpRequest req = Requests.createGetRequest("mydomain.com", "/public/asyncMeta.txt");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
	
	@Test
	public void testStaticDirFromDomain2() {
		HttpRequest req = Requests.createGetRequest("domain2.com", "/public/asyncMeta.txt");
		
		httpsSocket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(httpsSocket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver");
	}
}
