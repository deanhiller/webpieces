package org.webpieces.webserver.staticpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.net.URLEncoder;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestStaticPaths {

	private RequestListener server;
	private MockResponseSender socket = new MockResponseSender();
	private File cacheDir;

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("staticMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		cacheDir = webserver.getCacheDir();
		server = webserver.start();
	}

	@Test
	public void testStaticDir() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/staticMeta.txt");

		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver.staticpath.app.StaticMeta");
		response.assertContentType("text/plain; charset=utf-8");
	}

	@Test
	public void testStaticDirWithHashGeneration() throws FileNotFoundException, IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/pageparam");

		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);

		String hash = loadUrlEncodedHash();
		
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("/public/fonts.css?hash="+hash);
	}

	@Test
	public void testStaticDirWithHashLoad() throws FileNotFoundException, IOException {
		String hash = loadUrlEncodedHash();		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css?hash="+hash);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("themes.googleusercontent.com");
		response.assertContentType("text/css; charset=utf-8");
	}
	
	private String loadUrlEncodedHash() throws IOException, FileNotFoundException {
		File meta = new File(cacheDir, "public/webpiecesMeta.properties");
		Properties p = new Properties();
		p.load(new FileInputStream(meta));
		String hash = p.getProperty("/public/fonts.css");
		String encodedHash = URLEncoder.encode(hash);
		return encodedHash;
	}
	
	@Test
	public void testStaticDirWithBadHashDoesNotLoadMismatchFileIntoBrowser() throws FileNotFoundException, IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css?hash=BADHASH");

		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testStaticDirJpg() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/image.jpg");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContentType("image/jpeg");
		int size = response.getBody().getReadableSize();
		Assert.assertEquals(18066, size);
	}
	
	@Test
	public void testStaticDirAndNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/notFound.jpg");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		//render html page when not found...
		response.assertContentType("text/html; charset=utf-8");
	}
	
	@Test
	public void testStaticFile() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/myfile");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("app.TagsMeta");
	}
	
}
