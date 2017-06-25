package org.webpieces.webserver.tags;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;


public class TestListGenerator extends AbstractWebpiecesTest {

	
	private HttpSocket http11Socket;
	
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testListTag() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/list");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<td>dean</td><td>5</td><td>red</td>");
		response.assertContains("<td>jeff</td><td>2</td><td>blue</td");
		response.assertNotContains("You have no accounts at this time");
	}
	
	@Test
	public void testEmptyList() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/emptyList");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertNotContains("<td>dean</td><td>5</td><td>red</td>");
		response.assertNotContains("<td>jeff</td><td>2</td><td>blue</td");
		response.assertContains("You have no accounts at this time");
	}
}
