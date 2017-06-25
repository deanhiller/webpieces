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

public class TestBootstrapTag extends AbstractWebpiecesTest {

	
	private Http11Socket http11Socket;
	
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = http11Simulator.createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testBootstrap() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/bootstrap");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("$(`#editLink_2`).click(function(e){".replaceAll("`", "\""));
		response.assertContains("$('#addEditModal').load('/user/2', function(response, status, xhr){");
		response.assertContains("$(`#editLink_4`).click(function(e){".replaceAll("`", "\""));
		response.assertContains("$('#addEditModal').load('/user/2', function(response, status, xhr){");
	}
	
}
