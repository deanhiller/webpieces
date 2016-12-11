package org.webpieces.webserver.staticpath;

import java.util.List;

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
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestStaticPaths {

	private RequestListener server;
	private MockResponseSender mockResponseSender = new MockResponseSender();

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("staticMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testStaticDir() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/staticMeta.txt");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses(2000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("org.webpieces.webserver.staticpath.app.StaticMeta");
		response.assertContentType("text/plain; charset=utf-8");
	}
	
	@Test
	public void testStaticDirJpg() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/image.jpg");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses(2000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContentType("image/jpeg");
		int size = response.getBody().getReadableSize();
		Assert.assertEquals(18066, size);
	}
	
	@Test
	public void testStaticDirAndNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/pics.ext/notFound.jpg");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses(2000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		//render html page when not found...
		response.assertContentType("text/html; charset=utf-8");
	}
	
	@Test
	public void testStaticFile() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/public/myfile");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses(2000, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("app.TagsMeta");
	}
	
}
