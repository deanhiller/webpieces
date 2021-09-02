package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientConfig;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.json.app.EchoStreamingClient;
import org.webpieces.webserver.json.app.FakeAuthService;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http2.AbstractHttp2Test;
import org.webpieces.webserver.test.http2.Requests;
import org.webpieces.webserver.test.http2.TestMode;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

//
//TODO(dhiller): this was not finished and is not functional yet
//
//
//
//
//
//
//
//
//
//
//

@RunWith(Parameterized.class)
public class TestStreaming extends AbstractHttp2Test {
	
	private static final Logger log = LoggerFactory.getLogger(TestStreaming.class);
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
	

	public TestStreaming(TestMode testMode, Boolean isHttp1Protocal) {
		this.testMode = testMode;
		this.isHttp1Protocal = isHttp1Protocal;
		log.info("constructing test suite for client isRemote="+testMode);
	}
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("jsonMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(new SimpleMeterRegistry()), new TestOverrides(), true, metaFile);
		webserver.start();
		http2Socket = connectHttp(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testAsyncJsonGet() {
		String bodyStr = "asdlfkjsldfkjlsfkjdlksjfffffflsdkjfffffldksjflkdjsfldsjf";
		byte[] bytes = bodyStr.getBytes();
		DataWrapper body = gen.wrapByteArray(bytes);
		Http2Request request = Requests.createRequest("/json/streaming", body);
		
		RequestStreamHandle stream = http2Socket.openStream();
		
		StreamRef ref = stream.process(request, mockResponseListener );
		CompletableFuture<StreamWriter> writer = ref.getWriter();		
		
		
	}


	
	private class TestOverrides implements Module {

		@Override
		public void configure(Binder binder) {
			binder.bind(FakeAuthService.class).toInstance(mockAuth);
			binder.bind(EchoStreamingClient.class).toInstance(mockStreamClient);
			
		}
		
	}

	
}
