package WEBPIECESxPACKAGE;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

import WEBPIECESxPACKAGE.base.example.RemoteService;
import WEBPIECESxPACKAGE.mock.MockRemoteSystem;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class TestLesson1BasicRequestResponse {

	private final static Logger log = LoggerFactory.getLogger(TestLesson1BasicRequestResponse.class);
	
	private RequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockResponseSender that would follow
	//any redirects in the application properly..
	private MockResponseSender mockResponseSender = new MockResponseSender();
	//see below comments in AppOverrideModule
	private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library

	private JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
	private static String PU = HibernatePlugin.PERSISTENCE_TEST_UNIT;
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		log.info("Setting up test");
		Asserts.assertWasCompiledWithParamNames("test");
		
		//clear in-memory database
		jdbc.dropAllTablesFromDatabase();
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WEBPIECESxCLASSServer webserver = new WEBPIECESxCLASSServer(
				new PlatformOverridesForTest(), new AppOverridesModule(), new ServerConfig(PU));
		server = webserver.start();
	}
	
	/**
	 * Testing a synchronous controller may be easier especially if there is no remote communication.
	 */
	@Test
	public void testSynchronousController() {
		HttpRequest req = createRequest("/");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_200_OK);
	}
	
	/**
	 * It is highly suggested you step through this test in debug mode to understand the description below...
	 * 
	 * This is a single threaded test that actually allows the webserver thread to return back to the test before 
	 * the response comes.  (in production the thread would process other requests while waiting for remote system response).  
	 * Then the test simulates the response coming in from remote system and makes sure we send a response back
	 * to the ResponseSender.  In implementations like this with a remote system, one can avoid holding threads up
	 * and allow them to keep working while waiting for a response from the remote system.
	 */
	@Test
	public void testAsyncControllerAndRemoteSystem() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockRemote.addValueToReturn(future);
		HttpRequest req = createRequest("/async");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(0, responses.size());

		//notice that the thread returned but there is no response back to browser yet such that thread can do more work.
		//next, simulate remote system returning a value..
		int value = 85;
		future.complete(value);

		List<FullResponse> responses2 = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses2.size());
		
		FullResponse httpPayload = responses2.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		httpPayload.assertContains("This is a page with value="+value);
	}

	/**
	 * For the heck of it, test out chunked compressed response...
	 */
	@Test
	public void testChunkedCompression() {
		HttpRequest req = createRequest("/");
		req.addHeader(new Header(KnownHeaderName.ACCEPT_ENCODING, "gzip, deflate"));
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse httpPayload = responses.get(0);
		httpPayload.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Assert.assertTrue(httpPayload.getChunks().size() > 0);
		httpPayload.uncompressBodyAndAssertContainsString("Webpieces");
	}
	
	static HttpRequest createRequest(String uri) {
		HttpRequestLine requestLine = new HttpRequestLine();
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
