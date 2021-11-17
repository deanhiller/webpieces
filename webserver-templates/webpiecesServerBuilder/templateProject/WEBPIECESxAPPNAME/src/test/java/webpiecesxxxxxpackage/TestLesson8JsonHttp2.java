package webpiecesxxxxxpackage;

import java.io.IOException;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientConfig;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.http2.AbstractHttp2Test;
import org.webpieces.webserver.test.http2.ResponseWrapperHttp2;
import org.webpieces.webserver.test.http2.TestMode;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import org.webpieces.http.StatusCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import webpiecesxxxxxpackage.json.SearchRequest;
import webpiecesxxxxxpackage.json.SearchResponse;
import webpiecesxxxxxpackage.mock.JavaCache;
import webpiecesxxxxxpackage.mock.MockRemoteService;
import webpiecesxxxxxpackage.service.RemoteService;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class TestLesson8JsonHttp2 extends AbstractHttp2Test {

	private final static Logger log = LoggerFactory.getLogger(TestLesson8JsonHttp2.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private String[] args = { "-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory", "-hibernate.loadclassmeta=true" };
	private Http2Socket http2Socket;
	private ObjectMapper mapper = new ObjectMapper();
	private SimpleMeterRegistry metrics;


	@Override
	protected TestMode getTestMode() {
		//This mode slows things down but you can step through client and server http2 parser/hpack/engine etc.
		//If you are a beginner run in EMBEDDED_DIRET_NO_PARSING mode as it's faster AND you can step through 
		//the main stuff you care about(not the http2 protocol stuff).
		return TestMode.EMBEDDED_DIRET_NO_PARSING;
	}

	//The default in superclass is an http2 client on top of an http1.1 protocol.
	//by overridding here, we can use an http2 client on http2 protocol ONLY IF isRemote() returns true
	@Override
	protected Http2Client createRemoteClient() {
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		Http2ClientConfig config = new Http2ClientConfig();
		return Http2ClientFactory.createHttpClient(config, metrics);		
	}

	@Before
	public void setUp() {
		log.info("Setting up test");
		//This line is not really needed but ensures you do not run a test without param names compiled in(which will fail).
		Asserts.assertWasCompiledWithParamNames("test");
		
		metrics = new SimpleMeterRegistry();
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		Server webserver = new Server(getOverrides(metrics), new AppOverridesModule(), 
			new ServerConfig(JavaCache.getCacheLocation()), args
		);
		webserver.start();
		http2Socket = connectHttp(webserver.getUnderlyingHttpChannel().getLocalAddress());
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
		
		//check metrics are wired correctly here as well
		RequiredSearch result = metrics.get("testCounter");
		Counter counter = result.counter();
		Assert.assertEquals(1.0, counter.count(), 0.1);
	}
	
	private SearchResponse search(SearchRequest searchReq) {
		try {
			FullRequest req = createHttpRequest(searchReq);
	
			XFuture<FullResponse> respFuture = http2Socket.send(req);
			
			return waitAndTranslateResponse(respFuture);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private SearchResponse waitAndTranslateResponse(XFuture<FullResponse> respFuture)
			throws InterruptedException, ExecutionException, TimeoutException, IOException, JsonParseException,
			JsonMappingException {
		
		ResponseWrapperHttp2 response = ResponseExtract.waitAndWrap(respFuture);
		response.assertStatusCode(StatusCode.HTTP_200_OK);
		
		FullResponse fullResponse = respFuture.get(2, TimeUnit.SECONDS);
		DataWrapper data2 = fullResponse.getPayload();
		byte[] respContent = data2.createByteArray();
		return mapper.readValue(respContent, SearchResponse.class);
	}

	private FullRequest createHttpRequest(SearchRequest searchReq)
			throws IOException, JsonGenerationException, JsonMappingException {
		byte[] content = mapper.writeValueAsBytes(searchReq);
		DataWrapper data = dataGen.wrapByteArray(content);
		FullRequest req = createRequest("/json/556", data);
		return req;
	}
	
	public static FullRequest createRequest(String uri, DataWrapper body) {
		Http2Request req = new Http2Request();
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "yourdomain.com"));
		req.addHeader(new Http2Header(Http2HeaderName.SCHEME, "https"));
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, uri));
		req.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		
		FullRequest fullReq = new FullRequest(req, body, null);
		return fullReq;
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			binder.bind(RemoteService.class).toInstance(new MockRemoteService()); //see above comment on the field mockRemote
		}
	}
	
}
