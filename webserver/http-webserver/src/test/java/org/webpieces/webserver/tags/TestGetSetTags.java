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

public class TestGetSetTags {

	private MockFrontendSocket socket = new MockFrontendSocket();
	private HttpRequestListener server;
	
	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), null, false, null);
		server = webserver.start();
	}

	@Test
	public void testSimpleSetGet() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/setget");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("result=Jeff M");
		response.assertContains("key not exist so body is printed 'COOL' and test failfast:false works");
		response.assertContains("This is set body result='testing set body here Dean Hiller'");
		
		//tests whitespace cleaned up the line with #{set}# so these don't take up lines in the output
		response.assertContains("<body>\n    The above line should be cleared");
	}
	

}
