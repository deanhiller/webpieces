package org.webpieces.webserver.tags;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.ResponseExtract;


public class TestExtends extends AbstractWebpiecesTest {

	
	private HttpSocket http11Socket;
	
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testBasicExtends() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/extends");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        FullResponse response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("The body will be put here='Testing normal body='Dean Hiller''");
		response.assertContains("User is here='Dean Hiller'");
		response.assertContains("Body2='Test2'");
		response.assertContains("Body3=''");
		response.assertContains("Then anything not in a set will be assigned to 'body'");
		response.assertContains("SUPERTEMPLATE");
	}
	

}
