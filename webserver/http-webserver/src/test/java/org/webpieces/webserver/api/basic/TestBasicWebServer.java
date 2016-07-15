package org.webpieces.webserver.api.basic;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestBasicWebServer {

	private MockFrontendSocket socket = new MockFrontendSocket();
	
	@Test
	public void testBasic() {
		BasicWebserver webserver = new BasicWebserver(new PlatformOverridesForTest(), null);
		HttpRequestListener server = webserver.start();

		HttpRequest req = createRequest(KnownHttpMethod.GET, "/myroute");
		server.processHttpRequests(socket, req , false);
		
		List<HttpPayload> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		HttpResponse response = responses.get(0).getHttpResponse();
		DataWrapper body = response.getBody();
		String html = body.createStringFrom(0, body.getReadableSize(), StandardCharsets.UTF_8);
		Assert.assertTrue("payload="+html, html.contains("This is the first raw html page"));
	}

	private HttpRequest createRequest(KnownHttpMethod method, String url) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(method);
		requestLine.setUri(httpUri);
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		
		req.addHeader(new Header(KnownHeaderName.HOST, "myhost.com"));
		
		return req;
	}

}
