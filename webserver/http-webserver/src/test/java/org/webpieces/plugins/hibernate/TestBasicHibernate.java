package org.webpieces.plugins.hibernate;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestBasicHibernate {
	private MockFrontendSocket socket = new MockFrontendSocket();
	private HttpRequestListener server;

	@Before
	public void setUp() {
//		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
//		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
//		server = webserver.start();
	}
	
	@Test
	public void testCompletePromiseOnRequestThread() {
//		HttpRequest req = Requests.createRequest(KnownHttpMethod.POST, "/save");
//		
//		server.processHttpRequests(socket, req , false);
//		
//		List<FullResponse> responses = socket.getResponses();
//		Assert.assertEquals(1, responses.size());
//
//		FullResponse response = responses.get(0);
//		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
//		response.assertContains("This is the first raw html page");
	}
}
