package org.webpieces.webserver.https;

import static org.webpieces.httpparser.api.dto.HttpRequest.HttpScheme.HTTP;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestHttps {

	private RequestListener server;
	private MockResponseSender socket = new MockResponseSender();

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("httpsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testSecureLoginHasHttpsPage() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/internal", true); // https
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		//before we can show you the page, you need to be logged in, redirect to login page...
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
	
	@Test
	public void testBasicPageOverHttps() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secureRoute", true);
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("home page");
	}
	
	@Test
	public void testAccessHttpsPageOverHttp() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secureRoute");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		//Even though the page exists....if accessed over http, it does not exist...
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testSameRouteHttpAndHttpsWrongOrder() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same", true); // https
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route"); //notice the Https Route page is not shown		
		
		socket.clear();

		req.setHttpScheme(HTTP);
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route"); //notice the Https Route page is not shown	
	}

	@Test
	public void testSameRouteHttpAndHttpsCorrectOrder() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same2", true);
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Https Route");

		req.setHttpScheme(HTTP);
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route");
	}
	
	@Test
	public void testUseHttpButGoThroughLoginFilter() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/randomPage");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		//Even though the page exists....if accessed over http, it does not exist...
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);		
	}
	
	@Test
	public void testSecureLoginNotFoundHttpsPage() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/notFoundPage", true);
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		//Even though the page doesn't exist, we redirect all /secure/* to login page
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
	
	@Test
	public void testSecureAndLoggedInAlready() {
		Header cookie = simulateLogin();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/internal", true); // https
		req.addHeader(cookie);
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		//before we can show you the page, you need to be logged in, redirect to login page...
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is some home page");
	}

	@Test
	public void testReverseUrlLookupOnHttpPageForHttpsUrl() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same", false); // https
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("https://myhost.com:8443"); //notice the Https Route page is not shown	
	}

	@Test
	public void testReverseUrlLookupOnHttpPageForHttpsUrl8443() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same", false, 8080); // https
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("https://myhost.com:8443"); //notice the Https Route page is not shown	
	}
	
	private Header simulateLogin() {
		HttpRequest req1 = Requests.createRequest(KnownHttpMethod.POST, "/postLogin", true);
		
		server.incomingRequest(req1, new RequestId(0), true, socket);
		
		FullResponse response1 = ResponseExtract.assertSingleResponse(socket);
		Header header = response1.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
		String value = header.getValue();
		value = value.replace("; path=/; HttpOnly", "");
		Header cookie = new Header(KnownHeaderName.COOKIE, value);
		
		return cookie;
	}
}
