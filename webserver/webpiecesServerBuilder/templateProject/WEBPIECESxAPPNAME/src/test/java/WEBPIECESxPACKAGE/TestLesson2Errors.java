package WEBPIECESxPACKAGE;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

import WEBPIECESxPACKAGE.example.RemoteService;
import WEBPIECESxPACKAGE.example.SomeLibrary;
import WEBPIECESxPACKAGE.mock.MockRemoteSystem;
import WEBPIECESxPACKAGE.mock.MockSomeLibrary;

/**
 * Error/Failure testing is something that tends to get missed but it can be pretty important to make sure you render a nice message
 * when errors happen with links to other things.  The same goes for not found pages too so these are good tests to have/modify for
 * your use case.  I leave it to the test write to add one where rendering the 500 or 404 page fails ;).  On render 500 failure, our
 * platform swaps in a page of our own....ie. don't let your 500 page fail in the first place as our page does not match the style of
 * your website but at least let's the user know there was a bug (on top of a bug).
 * 
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test.
 * 
 * @author dhiller
 *
 */
public class TestLesson2Errors {

	private RequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockFrontendSocket that would follow
	//any redirects in the application properly..
	private MockFrontendSocket mockResponseSocket = new MockFrontendSocket();
	//see below comments in AppOverrideModule
	private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	private MockSomeLibrary mockLibrary = new MockSomeLibrary();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WEBPIECESxCLASSServer webserver = new WEBPIECESxCLASSServer(new PlatformOverridesForTest(), new AppOverridesModule(), new ServerConfig(0, 0));
		server = webserver.start();
	}
	
	/**
	 * This tests bug in your webapp "/another" route, you could also test you have a bug in that route AND a bug in your internal
	 * server route as well!!!
	 */
	@Test
	public void testWebAppHasBugRenders500Route() {
		mockLibrary.throwException(new RuntimeException("test internal bug page"));
		HttpRequest req = TestLesson1BasicRequestResponse.createRequest("/absolute");
		
		server.incomingRequest(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("You encountered a 5xx in your server");
	}
	
	/**
	 * You could also test notFound route fails with exception too...
	 */
	@Test
	public void testNotFound() {
		HttpRequest req = TestLesson1BasicRequestResponse.createRequest("/route/that/does/not/exist");
		
		server.incomingRequest(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		httpPayload.assertContains("Your page was not found");
	}
	
	/**
	 * Tests a remote asynchronous system fails and a 500 error page is rendered
	 */
	@Test
	public void testRemoteSystemDown() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockRemote.addValueToReturn(future);
		HttpRequest req = TestLesson1BasicRequestResponse.createRequest("/async");
		
		server.incomingRequest(mockResponseSocket, req, false);
		
		List<FullResponse> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses.size());

		//notice that the thread returned but there is no response back to browser yet such that thread can do more work.
		//next, simulate remote system returning a value..
		future.completeExceptionally(new RuntimeException("complete future with exception"));

		List<FullResponse> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses2.size());
		
		FullResponse httpPayload = responses2.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		httpPayload.assertContains("You encountered a 5xx in your server");
	}

	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			binder.bind(RemoteService.class).toInstance(mockRemote); //see above comment on the field mockRemote
			binder.bind(SomeLibrary.class).toInstance(mockLibrary);
		}
	}
	
}
