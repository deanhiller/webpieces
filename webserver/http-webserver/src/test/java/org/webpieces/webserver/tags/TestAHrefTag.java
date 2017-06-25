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


public class TestAHrefTag extends AbstractWebpiecesTest {

	
	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(getOverrides(false), null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testAHref() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/ahref");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        FullResponse response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<a href=`/verbatim` id=`myid`>My link2</a>".replace('`', '"'));
		response.assertContains("<a href=`/if`>My render link2</a>".replace('`', '"'));
		response.assertContains("<a href=`/else`>My full route link</a>".replace('`', '"'));
		response.assertContains("<a href=`/redirect/Dean+Hiller`>PureALink</a>".replace('`', '"'));
		response.assertContains("Link but no ahref='/redirect/Dean+Hiller'");
	}
	

}
