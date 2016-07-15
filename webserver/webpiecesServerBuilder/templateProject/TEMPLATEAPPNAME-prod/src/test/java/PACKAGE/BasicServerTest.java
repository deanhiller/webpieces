package PACKAGE;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

import PACKAGE.example.RemoteService;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class BasicServerTest {

	private HttpRequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockFrontendSocket that would follow
	//any redirects in the application properly..
	private MockFrontendSocket mockResponseSocket = new MockFrontendSocket();
	//see below comments in AppOverrideModule
	private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		TestBasicProductionStart.testWasCompiledWithParamNames("test");
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		CLASSNAMEServer webserver = new CLASSNAMEServer(new PlatformOverridesForTest(), new AppOverridesModule(), false, null);
		server = webserver.start();
	}
	
	/**
	 * Testing a synchronous controller may be easier especially if there is no remote communication.
	 */
	@Test
	public void testSynchronousController() {
		HttpRequest req = createRequest("/");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<HttpPayload> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		HttpPayload httpPayload = responses.get(0);
		HttpResponse httpResponse = httpPayload.getHttpResponse();
		Assert.assertEquals(KnownStatusCode.HTTP_200_OK, httpResponse.getStatusLine().getStatus().getKnownStatus());
	}
	
	/**
	 * This is a single threaded test that actually allows the webserver thread to return back to the test before 
	 * the response comes.  (in production the thread would process other requests).  Then it simulates the response
	 * coming in and makes sure we send a response back to the FrontendSocket.  In implementations like this with 
	 * a remote system, one can avoid holding threads up and allow them to keep working while waiting for a response
	 * from the remote system.
	 */
	@Test
	public void testAsyncControllerAndRemoteSystem() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockRemote.addValueToReturn(future);
		HttpRequest req = createRequest("/async");
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<HttpPayload> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(0, responses.size());

		//notice that the thread returned but there is no response back to browser yet such that thread can do more work.
		//next, simulate remote system returning a value..
		int value = 85;
		future.complete(value);

		List<HttpPayload> responses2 = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses2.size());
		
		HttpPayload httpPayload = responses2.get(0);
		HttpResponse httpResponse = httpPayload.getHttpResponse();
		Assert.assertEquals(KnownStatusCode.HTTP_200_OK, httpResponse.getStatusLine().getStatus().getKnownStatus());
		DataWrapper body = httpResponse.getBody();
		String html = body.createStringFrom(0, body.getReadableSize(), StandardCharsets.UTF_8);
		Assert.assertTrue("invalid html="+html, html.contains("This is a page with value="+value));
	}

	static HttpRequest createRequest(String uri) {
		HttpRequestLine requestLine = new HttpRequestLine();;
		requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri(uri));
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine );
		req.addHeader(new Header(KnownHeaderName.HOST, "yourdomain.com"));
		return req;
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			binder.bind(RemoteService.class).toInstance(mockRemote); //see above comment on the field mockRemote
		}
	}
	
}
