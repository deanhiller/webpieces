package org.webpieces.webserver.tags;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestStylesheetScriptTags {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testStylesheet() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/stylesheet");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<img alt=`hi` src=`public/pic.jpg`>".replace('`', '"'));
		response.assertContains("<link rel=`stylesheet` type=`text/css` href=`/public/fonts.css?hash=kxR6cr1IXKXcWyOAiVdRAQ%3D%3D` />".replace('`', '"'));
		response.assertContains("<script src=`public/jquery.js?hash=BIgcZnlh8tRzX%2BGIG5TESw%3D%3D`".replace('`', '"'));
	}
}
