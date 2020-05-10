package webpiecesxxxxxpackage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.PrecompressedCache;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import webpiecesxxxxxpackage.mock.MockRemoteSystem;
import webpiecesxxxxxpackage.mock.MockSomeLibrary;
import webpiecesxxxxxpackage.service.RemoteService;
import webpiecesxxxxxpackage.service.SomeLibrary;

/**
 * Error/Failure testing is something that tends to get missed but it can be pretty important to make sure you render a nice message
 * when errors happen with links to other things.  The same goes for not found pages too so these are good tests to have/modify for
 * your use case.  I leave it to the test sendResponse to add one where rendering the 500 or 404 page fails ;).  On render 500 failure, our
 * platform swaps in a page of our own....ie. don't let your 500 page fail in the first place as our page does not match the style of
 * your website but at least let's the user know there was a bug (on top of a bug).
 * 
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test.
 * 
 * @author dhiller
 *
 */
public class TestLesson3Errors extends AbstractWebpiecesTest {

	//see below comments in AppOverrideModule
	private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	private MockSomeLibrary mockLibrary = new MockSomeLibrary();
	private JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
	private String[] args = { "-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory", "-hibernate.loadclassmeta=true" };
	private HttpSocket http11Socket;
	private SimpleMeterRegistry metrics;
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		//clear in-memory database
		jdbc.dropAllTablesFromDatabase();
		
		metrics = new SimpleMeterRegistry();
		boolean isRemote = false;
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		Server webserver = new Server(getOverrides(isRemote, metrics), new AppOverridesModule(), new ServerConfig(PrecompressedCache.getCacheLocation()), args);
		webserver.start();
		http11Socket = connectHttp(isRemote, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	/**
	 * This tests bug in your webapp "/another" route, you could also test you have a bug in that route AND a bug in your internal
	 * server route as well!!!
	 */
	@Test
	public void testWebAppHasBugRenders500Route() {
		mockLibrary.addExceptionToThrow(() -> {
			throw new RuntimeException("test internal bug page");
		});
		HttpFullRequest req = TestLesson2Html.createRequest("/");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("You encountered a Bug in our web software");
	}
	
	/**
	 * You could also test notFound route fails with exception too...
	 */
	@Test
	public void testNotFound() {
		HttpFullRequest req = TestLesson2Html.createRequest("/route/that/does/not/exist");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your page was not found");
	}
	
	/**
	 * Tests a remote asynchronous system fails and a 500 error page is rendered
	 */
	@Test
	public void testRemoteSystemDown() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		mockRemote.addValueToReturn(future);
		HttpFullRequest req = TestLesson2Html.createRequest("/async");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());

		//notice that the thread returned but there is no response back to browser yet such that thread can do more work.
		//next, simulate remote system returning a value..
		future.completeExceptionally(new RuntimeException("complete future with exception"));

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains("You encountered a Bug in our web software");
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
