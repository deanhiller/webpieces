package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.json.app.FakeAuthService;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Binder;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@RunWith(Parameterized.class)
public class TestJsonStdFilter extends AbstractWebpiecesTest {
	
	private static final Logger log = LoggerFactory.getLogger(TestJsonStdFilter.class);
	private HttpSocket http11Socket;
	private MockAuthService mockSvc = new MockAuthService();

	private boolean isRemote;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
        List<Object[]> args = new ArrayList<Object[]>();
        args.add(new Object[] { false });
        args.add(new Object[] { true});
        
		return args;
	}
	 
	public TestJsonStdFilter(boolean isRemote) {
		this.isRemote = isRemote;
		log.info("constructing test suite for client isRemote="+isRemote);
	}
	
	private class TestOverrides implements com.google.inject.Module {

		@Override
		public void configure(Binder binder) {
			binder.bind(FakeAuthService.class).toInstance(mockSvc);
		}
		
	}
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("jsonMeta2.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(isRemote, new SimpleMeterRegistry()), new TestOverrides(), true, metaFile);
		webserver.start();
		http11Socket = connectHttp(isRemote, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testAsyncJsonPostContstraintViolation() {
		String json = "{ `query`: null, `meta`: { `numResults`: 4 } }".replace("`", "\"");
		HttpFullRequest req = Requests.createJsonRequest(KnownHttpMethod.POST, "/json/async/45", json);
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_400_BADREQUEST);
		response.assertContains("{`error`:`Your request is bad. Violation #1:'must not be blank' path=request.testValidation`,`code`:400,`serviceWithError`:`deansTestSvc`,`serviceFailureChain`:[`deansTestSvc`]}".replace("`", "\""));
		response.assertContentType("application/json");
	}
	
}
