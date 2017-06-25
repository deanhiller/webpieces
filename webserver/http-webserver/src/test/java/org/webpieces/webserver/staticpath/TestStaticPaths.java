package org.webpieces.webserver.staticpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.net.URLEncoder;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class TestStaticPaths extends AbstractWebpiecesTest {

	
	
	private File cacheDir;
	private Http11Socket http11Socket;

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("staticMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		cacheDir = webserver.getCacheDir();
		webserver.start();
		http11Socket = http11Simulator.createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testStaticDir() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/staticMeta.txt");

		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver.staticpath.app.StaticMeta");
		response.assertContentType("text/plain; charset=utf-8");
	}

	@Test
	public void testStaticDirWithHashGeneration() throws FileNotFoundException, IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/pageparam");

		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);

		String hash = loadUrlEncodedHash();
		
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("/public/fonts.css?hash="+hash);
	}

	@Test
	public void testStaticDirWithHashLoad() throws FileNotFoundException, IOException {
		String hash = loadUrlEncodedHash();		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css?hash="+hash);

		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
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
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css?hash=BADHASH");

		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testStaticDirJpg() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/image.jpg");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContentType("image/jpeg");
		int size = response.getBody().getReadableSize();
		Assert.assertEquals(18066, size);
	}
	
	@Test
	public void testStaticDirAndNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/notFound.jpg");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		//render html page when not found...
		response.assertContentType("text/html; charset=utf-8");
	}
	
	@Test
	public void testStaticFile() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/myfile");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("app.TagsMeta");
	}
	
	//a general test for testing if chunking is working or broken in relation to static files
	@Test
	public void testStaticFileCssLarger() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/mycss");
		
		http11Socket.send(req);
		
        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Open Sans");
	}
}
