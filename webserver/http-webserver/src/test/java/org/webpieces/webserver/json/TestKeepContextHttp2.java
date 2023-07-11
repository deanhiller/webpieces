package org.webpieces.webserver.json;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http.StatusCode;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientConfig;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.context.Context;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.json.app.EchoStreamingClient;
import org.webpieces.webserver.json.app.FakeAuthService;
import org.webpieces.webserver.json.app.SearchRequest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;
import org.webpieces.webserver.test.http2.AbstractHttp2Test;
import org.webpieces.webserver.test.http2.ResponseWrapperHttp2;
import org.webpieces.webserver.test.http2.TestMode;

import java.util.*;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RunWith(Parameterized.class)
public class TestKeepContextHttp2 extends AbstractHttp2Test {

	private static final Logger log = LoggerFactory.getLogger(TestKeepContextHttp2.class);
	private static DataWrapperGenerator gen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private Http2Socket http2Socket;
	private MockAuthService mockAuth = new MockAuthService();
	private MockStreamingClient mockStreamClient = new MockStreamingClient();
	private ResponseStreamHandle mockResponseListener = new MockResponseStreamHandle();

	private TestMode testMode;
	private Boolean isHttp1Protocal;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
        List<Object[]> args = new ArrayList<Object[]>();
        args.add(new Object[] { TestMode.EMBEDDED_PARSING, null });
        args.add(new Object[] { TestMode.EMBEDDED_DIRET_NO_PARSING, null });
        args.add(new Object[] { TestMode.REMOTE, true });
        args.add(new Object[] { TestMode.REMOTE, false });

		return args;
	}

	@Override
	protected TestMode getTestMode() {
		return testMode;
	}

	protected Http2Client createRemoteClient() {
		if(isHttp1Protocal == null)
			throw new UnsupportedOperationException("this should not be called since we are not remote");
		else if(isHttp1Protocal)
			return super.createRemoteClient();

		Http2ClientConfig c = new Http2ClientConfig();
		return Http2ClientFactory.createHttpClient(c, Metrics.globalRegistry);
	}


	public TestKeepContextHttp2(TestMode testMode, Boolean isHttp1Protocal) {
		this.testMode = testMode;
		this.isHttp1Protocal = isHttp1Protocal;
		log.info("constructing test suite for client isRemote="+testMode);
	}
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		//fails if another test is leaking the context...
		Assert.assertEquals(new HashMap(), Context.getContext());

		VirtualFileClasspath metaFile = new VirtualFileClasspath("jsonMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(new SimpleMeterRegistry()), new TestOverrides(), true, metaFile);
		webserver.start();
		http2Socket = connectHttp(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@After
	public void tearDown() {
		//clear client context
		Context.clear();
	}

	@Test
	public void testNoAttributeValueInJsonGoesToEmptyString() {
		String ctxKey = "test2";
		String ctxValue = "value2";
		String headerKey = "headerKey";
		String headerVal = "headerValue";
		String requestVal = "testRequestVal";

		//test out "something":null converts to "" in java....
		String json = "{ `meta`: { `numResults`: 4 }, `testValidation`:`notBlank` }".replace("`", "\"");
		FullRequest req = org.webpieces.webserver.test.http2.Requests.createJsonRequest("POST", "/json/simple", json);

		Context.put(ctxKey, ctxValue);

		//Have to also test request and headers SEPARATELY as test was passing until I added this
		Map<String, Object> headerMap = new HashMap<>();
		headerMap.put(headerKey, headerVal);
		Context.put(Context.REQUEST, requestVal);
		Context.put(Context.HEADERS, headerMap);

		XFuture<FullResponse> respFuture = http2Socket.send(req);
		ResponseWrapperHttp2 response = ResponseExtract.waitAndWrap(respFuture);

		//validate that Context was not blown away
		Assert.assertEquals(ctxValue, Context.get(ctxKey));

		Assert.assertEquals(requestVal, Context.get(Context.REQUEST));
		Map<String, Object> headers = (Map<String, Object>) Context.get(Context.HEADERS);
		Assert.assertEquals(headerVal, headers.get(headerKey));

		response.assertStatusCode(StatusCode.HTTP_200_OK);
		response.assertContentType("application/json");
	}

	@Test
	public void testSeperationOfMDCFromServer() {
		String mdcKey = "test";
		String mdcValue = "value";

		//test out "something":null converts to "" in java....
		String json = "{ `meta`: { `numResults`: 4 }, `testValidation`:`notBlank` }".replace("`", "\"");
		FullRequest req = org.webpieces.webserver.test.http2.Requests.createJsonRequest("POST", "/json/simple", json);

		//not exactly part of this test but checking for leak of server context into client
		// (only in embedded modes does this occur)
		Assert.assertEquals(0, Context.getContext().size());
		MDC.put(mdcKey, mdcValue);
		//MDC writes into XFutureMDCAdapter
		Assert.assertEquals(1, Context.getContext().size());

		XFuture<FullResponse> respFuture = http2Socket.send(req);
		ResponseWrapperHttp2 response = ResponseExtract.waitAndWrap(respFuture);

		//not exactly part of this test but checking for leak of server context into client
		// (only in embedded modes does this occur)
		Assert.assertEquals(1, Context.getContext().size());

		//validate that MDC was not blown away
		Assert.assertEquals(mdcValue, MDC.get(mdcKey));

		response.assertStatusCode(StatusCode.HTTP_200_OK);
		response.assertContentType("application/json");
	}

	
	private class TestOverrides implements Module {

		@Override
		public void configure(Binder binder) {
			binder.bind(FakeAuthService.class).toInstance(mockAuth);
			binder.bind(EchoStreamingClient.class).toInstance(mockStreamClient);
			
		}
		
	}

	
}
