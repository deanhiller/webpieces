package org.webpieces.webserver.staticpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
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
import org.webpieces.util.net.URLEncoder;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

//Run parameterized so that local and remote gets tested
@RunWith(Parameterized.class)
public class TestStaticPaths extends AbstractWebpiecesTest {
	
	private File cacheDir;
	private HttpSocket http11Socket;
	
	
	private boolean isRemote; //For this test and keep alives we run in embeedded mode AND FULL server across socket modes

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
		return Arrays.asList(new Object[][] {
	        { true },
	         { false }
	      });
	}
	
	public TestStaticPaths(boolean isRemote) {
		this.isRemote = isRemote;
	}
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("staticMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(isRemote, new SimpleMeterRegistry()), null, true, metaFile);
		cacheDir = webserver.getCacheDir();
		webserver.start();
		http11Socket = connectHttp(isRemote, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testStaticDir() throws InterruptedException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/staticMeta.txt");

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver.staticpath.app.StaticMeta");
		response.assertContentType("text/plain; charset=utf-8");
		
		if(!http11Socket.isClosed())
			Thread.sleep(2000); //we are across a second in one of the test cases so take a second to wait for close
		
		//there is no keep alive.  socket should be closed.
		Assert.assertTrue(http11Socket.isClosed());
	}

	@Test
	public void testStaticDirWithKeepAlive() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/staticMeta.txt");
		req.addHeader(new Header(KnownHeaderName.CONNECTION, "keep-alive"));

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver.staticpath.app.StaticMeta");
		response.assertContentType("text/plain; charset=utf-8");
		
		//there is a keep alive.  socket should be open
		Assert.assertFalse(http11Socket.isClosed());
	}
	

	@Test
	public void testStaticDirWithHashGeneration() throws FileNotFoundException, IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/pageparam");

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		String hash = loadUrlEncodedHash();
		
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("/public/fonts.css?hash="+hash);
	}

	@Test
	public void testStaticDirWithHashLoad() throws FileNotFoundException, IOException {
		String hash = loadUrlEncodedHash();		
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css?hash="+hash);

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("themes.googleusercontent.com");
		response.assertContentType("text/css; charset=utf-8");
	}
	
	private String loadUrlEncodedHash() throws IOException, FileNotFoundException {
		File meta = new File(cacheDir, "public/webpiecesMeta.properties");
		Properties p = new Properties();
		p.load(new FileInputStream(meta));
		String hash = p.getProperty("/public/fonts.css");
		String encodedHash = URLEncoder.encode(hash, StandardCharsets.UTF_8);
		return encodedHash;
	}
	
	@Test
	public void testStaticDirWithBadHashDoesNotLoadMismatchFileIntoBrowser() throws FileNotFoundException, IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css?hash=BADHASH");

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testStaticDirJpg() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/image.jpg");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContentType("image/jpeg");
		int size = response.getBody().getReadableSize();
		Assert.assertEquals(18066, size);
	}
	
	@Test
	public void testStaticDirAndNotFound() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/notFound.jpg");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		//render html page when not found...
		response.assertContentType("text/html; charset=utf-8");
	}
	
	@Test
	public void testStaticFile() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/myfile");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("app.TagsMeta");
	}
	
	//a general test for testing if chunking is working or broken in relation to static files
	@Test
	public void testStaticFileCssLarger() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/mycss");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Open Sans");
	}

	@Test
	public void testClasspathDirJpb() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/testclasspath/image.jpg");

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContentType("image/jpeg");
		int size = response.getBody().getReadableSize();
		Assert.assertEquals(18066, size);
	}
}
