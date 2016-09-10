package org.webpieces.webserver.https;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestHttps {

	private HttpRequestListener server;
	private MockFrontendSocket socket = new MockFrontendSocket();

	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig();
		VirtualFileClasspath metaFile = new VirtualFileClasspath("httpsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testSameRouteHttpAndHttpsWrongOrder() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same");
		
		server.processHttpRequests(socket, req , true); //https
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route"); //notice the Https Route page is not shown		
		
		socket.clear();
		
		server.processHttpRequests(socket, req , false); //http
		
		responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route"); //notice the Https Route page is not shown	
	}
	
	@Test
	public void testSameRouteHttpAndHttpsCorrectOrder() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/same2");
		
		server.processHttpRequests(socket, req , true);
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Https Route");
		
		socket.clear();
		
		server.processHttpRequests(socket, req , false);
		
		responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Http Route");
	}
	
	@Test
	public void testBasicPageOverHttps() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secureRoute");
		
		server.processHttpRequests(socket, req , true);
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("home page");
	}
	
	@Test
	public void testAccessHttpsPageOverHttp() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secureRoute");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		//Even though the page exists....if accessed over http, it does not exist...
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testUseHttpButGoThroughLoginFilter() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/randomPage");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		//Even though the page exists....if accessed over http, it does not exist...
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);		
	}
	
	@Test
	public void testSecureLoginNotFoundHttpsPage() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/notFoundPage");
		
		server.processHttpRequests(socket, req , true);
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		//Even though the page doesn't exist, we redirect all /secure/* to login page
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
	
	@Test
	public void testSecureLoginHasHttpsPage() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/secure/internal");
		
		server.processHttpRequests(socket, req , true);
		
		List<FullResponse> responses = socket.getResponses(200000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		//before we can show you the page, you need to be logged in, redirect to login page...
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
}
