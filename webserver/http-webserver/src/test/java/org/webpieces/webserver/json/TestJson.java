package org.webpieces.webserver.json;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;
import org.webpieces.webserver.test.HttpDummyRequest;

public class TestJson extends AbstractWebpiecesTest {
	
	private Http11Socket http11Socket;

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("jsonMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = http11Simulator.openHttp();
	}

	@Test
	public void testAsyncJsonGet() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/async/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:8,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testAsyncBadJsonGet() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.GET, "/json/async/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testAsyncJsonPost() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/async/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:98,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}

	@Test
	public void testAsyncWriteOnlyPost() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/writeasync");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Assert.assertEquals("", response.getBodyAsString());
	}
	
	@Test
	public void testAsyncBadJsonPost() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/async/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncJsonGet() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:5,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	//had a bug on this one so add a test
	@Test
	public void testSimulateCurl() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/45");
		req.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:5,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncBadJsonGet() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.GET, "/json/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncJsonPost() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:99,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncWriteOnlyPost() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/write");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Assert.assertEquals("", response.getBodyAsString());
	}
	
	@Test
	public void testSyncBadJsonPost() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/45");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testNotFoundInJsonUrls() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/some/notexist/route");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("{`error`:`This url has no api.  try another url`,`code`:0}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testNotFoundInHtmlUrls() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/html");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
		response.assertContentType("text/html; charset=utf-8");
	}
	
	@Test
	public void testRouteParamConversionFail() {
		HttpDummyRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/somenotexistroute");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND); //clearly this url has nothing there
		response.assertContains("{`error`:`This url has no api.  try another url`,`code`:0}".replace("`", "\""));
		response.assertContentType("application/json");
	}

	@Test
	public void testControllerThrowsNotFound() {
		HttpDummyRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/throw/333");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND); //clearly this url has nothing there
		response.assertContains("{`error`:`This url has no api.  try another url`,`code`:0}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testReadOnly() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/json/read");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:1,`matches`:[]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
}
