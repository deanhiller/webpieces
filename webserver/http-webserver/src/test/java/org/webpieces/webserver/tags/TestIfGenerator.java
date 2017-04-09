package org.webpieces.webserver.tags;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestIfGenerator {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testIfTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/if");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This should exist");
		response.assertNotContains("Negative1");
		response.assertNotContains("Negative2");
	}
	
	@Test
	public void testElseTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/else");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertNotContains("This should not exist");
		response.assertContains("This should exist");
		response.assertContains("Else1");
		response.assertContains("Else2");
		response.assertContains("Else3");
		
		//add an assert for comments being taken out so this test will break as this can be nice for readability
		response.assertContains("<body>\n    Testing for else");
	}	

	@Test
	public void testElseIFTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/elseif");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertNotContains("This should not exist");
		response.assertContains("This should exist");
		response.assertContains("ElseIf1");
	}
}
