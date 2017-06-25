package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.ResponseExtract;

@RunWith(Parameterized.class)
public class TestJson extends AbstractWebpiecesTest {
	
	private static final Logger log = LoggerFactory.getLogger(TestJson.class);
	private HttpSocket http11Socket;

	private boolean isRemote;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
        List<Object[]> args = new ArrayList<Object[]>();
        args.add(new Object[] { true });
        args.add(new Object[] { false});
        
		return args;
	}
	 
	public TestJson(boolean isDirect) {
		this.isRemote = isDirect;
		log.info("constructing test suite for client isRemote="+isDirect);
	}
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("jsonMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(getOverrides(isRemote), null, true, metaFile);
		webserver.start();
		http11Socket = connectHttp(isRemote, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testAsyncJsonGet() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/async/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:8,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testAsyncBadJsonGet() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.GET, "/json/async/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testAsyncJsonPost() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/async/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:98,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}

	@Test
	public void testAsyncWriteOnlyPost() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/writeasync");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Assert.assertEquals("", response.getBodyAsString());
	}
	
	@Test
	public void testAsyncBadJsonPost() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/async/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncJsonGet() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:5,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	//had a bug on this one so add a test
	@Test
	public void testSimulateCurl() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/45");
		req.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:5,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncBadJsonGet() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.GET, "/json/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncJsonPost() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:99,`matches`:[`match1`,`match2`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testSyncWriteOnlyPost() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/write");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Assert.assertEquals("", response.getBodyAsString());
	}
	
	@Test
	public void testSyncBadJsonPost() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/45");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`invalid json in client request.  Unexpected character ('c' (code 99)): was expecting a colon to separate field name and value".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testNotFoundInJsonUrls() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/some/notexist/route");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("{`error`:`This url has no api.  try another url`,`code`:0}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testNotFoundInHtmlUrls() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/html");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
		response.assertContentType("text/html; charset=utf-8");
	}
	
	@Test
	public void testRouteParamConversionFail() {
		HttpFullRequest req = Requests.createBadJsonRequest(KnownHttpMethod.POST, "/json/somenotexistroute");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND); //clearly this url has nothing there
		response.assertContains("{`error`:`This url has no api.  try another url`,`code`:0}".replace("`", "\""));
		response.assertContentType("application/json");
	}

	@Test
	public void testControllerThrowsNotFound() {
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/throw/333");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND); //clearly this url has nothing there
		response.assertContains("{`error`:`This url has no api.  try another url`,`code`:0}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
	@Test
	public void testReadOnly() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/json/read");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("{`searchTime`:1,`matches`:[]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
}
