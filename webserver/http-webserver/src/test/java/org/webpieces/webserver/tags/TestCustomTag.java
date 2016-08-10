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
import org.webpieces.webserver.api.TagOverridesModule;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class TestCustomTag {

	private MockFrontendSocket socket = new MockFrontendSocket();
	private HttpRequestListener server;
	
	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		Module allOverrides = Modules.combine(new PlatformOverridesForTest(config), new TagOverridesModule(TestTagOverrideLookup.class));
		WebserverForTest webserver = new WebserverForTest(allOverrides, null, false, null);
		server = webserver.start();
	}

	@Test
	public void testCustomTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/customtag");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Page Using Custom Tag");
		response.assertContains("This is a custom tag which can also use tags in itself <a href=`/verbatim`>Some Link Here</a>".replace('`', '"'));
		response.assertContains("The user is Dean Hiller and tag argument cool"); //using variable in custom tag
		response.assertContains("After Custom Tag");
	}
	

}
