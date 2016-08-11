package org.webpieces.webserver.tags;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestFieldTag {

	private MockFrontendSocket socket = new MockFrontendSocket();
	private HttpRequestListener server;
	
	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), null, false, null);
		server = webserver.start();
	}

	@Test
	public void testBasic() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/customFieldTag");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<div class=`controls`><input type=`text` name=`user` value=`Dean` class=`input-xlarge`/><span class=`errorMsg`></span></div>".replace('`', '"'));
	}

}
