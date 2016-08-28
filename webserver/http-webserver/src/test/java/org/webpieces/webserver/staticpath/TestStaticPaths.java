package org.webpieces.webserver.staticpath;

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

public class TestStaticPaths {

	private HttpRequestListener server;
	private MockFrontendSocket socket = new MockFrontendSocket();

	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		VirtualFileClasspath metaFile = new VirtualFileClasspath("staticMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testStaticDir() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/staticMeta.txt");
		
//		server.processHttpRequests(socket, req , false);
//		
//		List<FullResponse> responses = socket.getResponses(2000, 1);
//		Assert.assertEquals(1, responses.size());
//
//		FullResponse response = responses.get(0);
//		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
//		response.assertContains("Hi Dean Hiller, this is testing");
//		response.assertContentType("some text");
	}

//	@Test
//	public void testStaticFile() {
//		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/myfile");
//		
//		server.processHttpRequests(socket, req , false);
//		
//		List<FullResponse> responses = socket.getResponses();
//		Assert.assertEquals(1, responses.size());
//
//		FullResponse response = responses.get(0);
//		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
//		response.assertContains("Hi Dean Hiller, this is testing");
//	}
	
}
