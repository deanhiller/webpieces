package org.webpieces.webserver.tags;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

public class TestStylesheetScriptTags extends AbstractWebpiecesTest {

	
	private Http11Socket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = http11Simulator.createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testStylesheet() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/stylesheet");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<img alt=`hi` src=`public/pic.jpg`>".replace('`', '"'));
		response.assertContains("<link rel=`stylesheet` type=`text/css` href=`/public/fonts.css?hash=kxR6cr1IXKXcWyOAiVdRAQ%3D%3D` />".replace('`', '"'));
		response.assertContains("<script src=`public/jquery.js?hash=BIgcZnlh8tRzX%2BGIG5TESw%3D%3D`".replace('`', '"'));
	}

}
