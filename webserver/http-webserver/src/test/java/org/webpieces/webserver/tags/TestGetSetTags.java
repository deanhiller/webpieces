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


public class TestGetSetTags extends AbstractWebpiecesTest {

	
	private HttpSocket http11Socket;
	
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testSimpleSetGet() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/setget");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        FullResponse response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("result=Jeff M");
		response.assertContains("key not exist so body is printed 'COOL' and test failfast:false works");
		response.assertContains("This is set body result='testing set body here Dean Hiller'");
		
		//tests whitespace cleaned up the line with #{set}# so these don't take up lines in the output
		response.assertContains("<body>\n    The above line should be cleared");
		
		response.assertContains("&lt;a href=&quot;&quot;/&gt;"); //ensure we properly escape html from get/set
		response.assertContains("<h2>Title2</h2>"); //ensure we use escape:false		
		
		//by default we do not escape the body of set when using get as it is used heavily in
		//templating.  to avoid this if you like, there is an escapeHtml on the set element
		response.assertContains("<h1 class=\"\">Some Header</h1>");
		response.assertContains("&lt;h3 class=&quot;&quot;&gt;Header3&lt;/h1&gt;"); //ensure we use escape:true
	}
	

}
