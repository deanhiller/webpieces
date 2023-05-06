package webpiecesxxxxxpackage;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.futures.XFuture;
import webpiecesxxxxxpackage.deleteme.api.SearchRequest;
import webpiecesxxxxxpackage.deleteme.api.SearchResponse;
import webpiecesxxxxxpackage.deleteme.remoteapi.FetchValueResponse;
import webpiecesxxxxxpackage.framework.FeatureTest;
import webpiecesxxxxxpackage.framework.Requests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class TestMicroSvcApi extends FeatureTest {

	private final static Logger log = LoggerFactory.getLogger(TestMicroSvcApi.class);

	/**
	 * Testing a synchronous controller may be easier especially if there is no remote communication.
	 */
	@Test
	public void testSynchronousController() throws ExecutionException, InterruptedException, TimeoutException {
		//move complex request building out of the test...
		SearchRequest req = Requests.createSearchRequest();

		mockRemoteService.addValueToReturn(XFuture.completedFuture(new FetchValueResponse()));

		//always call the client api we test in the test method so developers can find what we test
		//very easily.. (do not push this down behind a method as we have found it slows others down
		//and is the whole key point of the test)
		//Think of writing a book and the table of contents(TOC) and saveApi.search is in the middle of
		//the TOC with a big huge section of code under it(arguable the biggest since validate() will be small)
		SearchResponse resp = saveApi.search(req).get(5, TimeUnit.SECONDS);

		validate(resp);
	}

	private void validate(SearchResponse resp) {
		Assert.assertEquals(5, resp.getSearchTime());
	}


}
