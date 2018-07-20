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
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;


public class TestIfGenerator extends AbstractWebpiecesTest {

	
	private HttpSocket http11Socket;
	
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false), null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testIfTag() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/if");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This should exist");
		response.assertNotContains("Negative1");
		response.assertNotContains("Negative2");
	}
	
	@Test
	public void testElseTag() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/else");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
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
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/elseif");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertNotContains("This should not exist");
		response.assertContains("This should exist");
		response.assertContains("ElseIf1");
	}
}
