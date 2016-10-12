package org.webpieces.webserver.tags;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestExtends {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig();
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testBasicExtends() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/extends");
		
		server.incomingRequest(req, true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("The body will be put here='Testing normal body='Dean Hiller''");
		response.assertContains("User is here='Dean Hiller'");
		response.assertContains("Body2='Test2'");
		response.assertContains("Body3=''");
		response.assertContains("Then anything not in a set will be assigned to 'body'");
		response.assertContains("SUPERTEMPLATE");
	}
	

}
