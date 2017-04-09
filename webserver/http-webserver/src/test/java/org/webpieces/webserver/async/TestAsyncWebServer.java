package org.webpieces.webserver.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestAsyncWebServer {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	private MockSomeOtherLib mockNotFoundLib = new MockSomeOtherLib();

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("asyncMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), new AppOverridesModule(), false, metaFile);
		server = webserver.start();		
	}
	
	@Test
	public void testCompletePromiseOnRequestThread() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/myroute");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the first raw html page");
	}
	
	@Test
	public void testCompletePromiseOnAnotherThread() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockNotFoundLib.queueFuture(future );
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/asyncSuccessRoute");

		server.incomingRequest(req, new RequestId(0), true, socket);

		//no response yet...
		List<FullResponse> responses1 = socket.getResponses();
		Assert.assertEquals(0, responses1.size());
		
		//now have the server complete processing
		future.complete(5);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Hi Dean Hiller, This is a page");
	}
	
	@Test
	public void testRedirect() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
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
