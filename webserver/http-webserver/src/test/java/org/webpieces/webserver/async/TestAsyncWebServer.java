package org.webpieces.webserver.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestAsyncWebServer {

	private MockFrontendSocket socket = new MockFrontendSocket();
	private HttpRequestListener server;
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();

	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("asyncMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), new AppOverridesModule(), false, metaFile);
		server = webserver.start();		
	}
	
	@Test
	public void testCompletePromiseOnRequestThread() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}
	
	@Test
	public void testCompletePromiseOnAnotherThread() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future );
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/asyncSuccessRoute");
		
		server.processHttpRequests(socket, req , false);

		//now have the server complete processing
		future.complete(5);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Hi Dean Hiller, This is a page");
	}
	
	@Test
	public void testRedirect() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(0, response.getBody().getReadableSize());
	}	

	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockNotFoundLib);
		}
	}
}
