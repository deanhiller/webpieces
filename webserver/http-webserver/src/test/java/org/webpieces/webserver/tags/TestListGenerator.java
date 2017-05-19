package org.webpieces.webserver.tags;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class TestListGenerator extends AbstractWebpiecesTest {

	
	private Http11Socket http11Socket;
	
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = http11Simulator.openHttp();
	}

	@Test
	public void testListTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/list");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<td>dean</td><td>5</td><td>red</td>");
		response.assertContains("<td>jeff</td><td>2</td><td>blue</td");
		response.assertNotContains("You have no accounts at this time");
	}
	
	@Test
	public void testEmptyList() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/emptyList");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertNotContains("<td>dean</td><td>5</td><td>red</td>");
		response.assertNotContains("<td>jeff</td><td>2</td><td>blue</td");
		response.assertContains("You have no accounts at this time");
	}
}
