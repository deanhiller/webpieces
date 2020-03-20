package org.webpieces.webserver.tokens;

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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class TestEscapeTokens extends AbstractWebpiecesTest {
	
	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tokensMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false), null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	
	@Test
	public void testEscapingTokens() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/escaping");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Html is escaped here=&lt;h1&gt;Some&amp; Title&lt;/h1&gt; by default");
		response.assertContains("This is not escaped same variable <h1>Some& Title</h1> here");
		
		//ensure maintain the newline
		response.assertContains("%{...}%  %[...]% ${...}$ #{...}# #{/...}# #{.../}#\n&amp;{...}&amp;  @[...]@  @@[...]@@  *{...}* *[");
		response.assertContains("&lt;a href=&quot;@[ACTION]@&quot;&gt;Link&lt;/a&gt;");
		
		//this was a comment in *{..}* that should not show up
		response.assertNotContains("escapes html as well so you can easily type raw html");

	}
}
