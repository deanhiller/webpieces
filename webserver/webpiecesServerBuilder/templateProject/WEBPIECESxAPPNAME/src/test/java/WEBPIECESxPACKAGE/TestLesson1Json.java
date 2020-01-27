package WEBPIECESxPACKAGE;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;

import com.google.inject.Binder;
import com.google.inject.Module;

import WEBPIECESxPACKAGE.base.json.SearchRequest;
import WEBPIECESxPACKAGE.base.json.SearchResponse;
import WEBPIECESxPACKAGE.base.libs.RemoteService;
import WEBPIECESxPACKAGE.mock.MockRemoteSystem;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class TestLesson1Json extends AbstractWebpiecesTest {

	private final static Logger log = LoggerFactory.getLogger(TestLesson1Json.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private String[] args = { "-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=hibernatefortest" };
	private HttpSocket http11Socket;
	private ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		log.info("Setting up test");
		//This line is not really needed but ensures you do not run a test without param names compiled in(which will fail).
		Asserts.assertWasCompiledWithParamNames("test");
		
		boolean isRemote = false; //you could parameterize the test and run remote or local
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		Server webserver = new Server(
			getOverrides(isRemote), new AppOverridesModule(), new ServerConfig(JavaCache.getCacheLocation()), args
		);
		webserver.start();
		http11Socket = connectHttp(isRemote, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	/**
	 * Testing a synchronous controller may be easier especially if there is no remote communication.
	 */
	@Test
	public void testSynchronousController() {
		SearchRequest req = new SearchRequest();
		req.setQuery("my query");

		SearchResponse resp = search(req);

		Assert.assertEquals("match1", resp.getMatches().get(0));
	}
	
	private SearchResponse search(SearchRequest searchReq) {
		try {
			HttpFullRequest req = createHttpRequest(searchReq);
	
			CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
			
			return waitAndTranslateResponse(respFuture);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private SearchResponse waitAndTranslateResponse(CompletableFuture<HttpFullResponse> respFuture)
			throws InterruptedException, ExecutionException, TimeoutException, IOException, JsonParseException,
			JsonMappingException {
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		
		HttpFullResponse fullResponse = respFuture.get(2, TimeUnit.SECONDS);
		DataWrapper data2 = fullResponse.getData();
		byte[] respContent = data2.createByteArray();
		return mapper.readValue(respContent, SearchResponse.class);
	}

	private HttpFullRequest createHttpRequest(SearchRequest searchReq)
			throws IOException, JsonGenerationException, JsonMappingException {
		byte[] content = mapper.writeValueAsBytes(searchReq);
		DataWrapper data = dataGen.wrapByteArray(content);
		HttpFullRequest req = createRequest("/json/556", data);
		return req;
	}
	
	public static HttpFullRequest createRequest(String uri, DataWrapper body) {
		HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri(uri));
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine );
		req.addHeader(new Header(KnownHeaderName.HOST, "yourdomain.com"));
		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		
		HttpFullRequest fullReq = new HttpFullRequest(req, body);
		return fullReq;
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			binder.bind(RemoteService.class).toInstance(new MockRemoteSystem()); //see above comment on the field mockRemote
		}
	}
	
}
