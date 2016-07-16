package org.webpieces.webserver.sync.error;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author dhiller
 *
 */
public class TestNotFoundAdvanced {

	private HttpRequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockFrontendSocket that would follow
	//any redirects in the application properly..
	private MockFrontendSocket mockResponseSocket = new MockFrontendSocket();
	//see below comments in AppOverrideModule
//	private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
//	private MockSomeLibrary mockLibrary = new MockSomeLibrary();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		VirtualFile metaFile = new VirtualFileClasspath("notfound.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), new AppOverridesModule(), false, metaFile);
		server = webserver.start();
	}
	
	@Test
	public void testNotFoundHandlerThrowsNotFound() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/route/that/does/not/exist");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("There was a bug bug bug in our software...sorry about that");
	}
	
	@Test
	public void testNotFoundThrowsException() {
	}
	
	@Test
	public void testNotFoundThrowsThenInternalSvrErrorHandlerThrows() {
	}
	
	/**
	 * This tests bug in your webapp "/another" route, you could also test you have a bug in that route AND a bug in your internal
	 * server route as well!!!
	 */
	@Test
	public void testWebAppHasBugRenders500Route() {
//		mockLibrary.throwException(new RuntimeException("test internal bug page"));
//		HttpRequest req = TestLesson1BasicRequestResponse.createRequest("/absolute");
//		
//		server.processHttpRequests(mockResponseSocket, req, false);
//		
//		List<HttpPayload> responses = mockResponseSocket.getResponses();
//		Assert.assertEquals(1, responses.size());
//
//		HttpPayload httpPayload = responses.get(0);
//		HttpResponse httpResponse = httpPayload.getHttpResponse();
//		Assert.assertEquals(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, httpResponse.getStatusLine().getStatus().getKnownStatus());
//		DataWrapper body = httpResponse.getBody();
//		String html = body.createStringFrom(0, body.getReadableSize(), StandardCharsets.UTF_8);
//		Assert.assertTrue("invalid html="+html, html.contains("You encountered a 5xx in your server"));
	}
	
	/**
	 * Tests a remote asynchronous system fails and a 500 error page is rendered
	 */
	@Test
	public void testRemoteSystemDown() {
//		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
//		mockRemote.addValueToReturn(future);
//		HttpRequest req = TestLesson1BasicRequestResponse.createRequest("/async");
//		
//		server.processHttpRequests(mockResponseSocket, req, false);
//		
//		List<HttpPayload> responses = mockResponseSocket.getResponses();
//		Assert.assertEquals(0, responses.size());
//
//		//notice that the thread returned but there is no response back to browser yet such that thread can do more work.
//		//next, simulate remote system returning a value..
//		future.completeExceptionally(new RuntimeException("complete future with exception"));
//
//		List<HttpPayload> responses2 = mockResponseSocket.getResponses();
//		Assert.assertEquals(1, responses2.size());
//		
//		HttpPayload httpPayload = responses2.get(0);
//		HttpResponse httpResponse = httpPayload.getHttpResponse();
//		Assert.assertEquals(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, httpResponse.getStatusLine().getStatus().getKnownStatus());
//		DataWrapper body = httpResponse.getBody();
//		String html = body.createStringFrom(0, body.getReadableSize(), StandardCharsets.UTF_8);
//		Assert.assertTrue("invalid html="+html, html.contains("You encountered a 5xx in your server"));
	}

	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
//			binder.bind(RemoteService.class).toInstance(mockRemote); //see above comment on the field mockRemote
//			binder.bind(SomeLibrary.class).toInstance(mockLibrary);
		}
	}
	
}
