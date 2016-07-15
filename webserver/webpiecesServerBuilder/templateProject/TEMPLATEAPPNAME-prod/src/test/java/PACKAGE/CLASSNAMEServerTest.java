package PACKAGE;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.webpieces.webserver.api.basic.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class CLASSNAMEServerTest {
	
	//see below comments in AppOverrideModule
	//private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	
	private HttpRequestListener server;
	//In the future, we may develop a FrontendSimulator that can be used instead of MockFrontendSocket that would follow
	//any redirects in the application properly..
	private MockFrontendSocket mockResponseSocket = new MockFrontendSocket();

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

		HttpRequestLine requestLine = new HttpRequestLine();;
		requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri("/"));
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine );
		req.addHeader(new Header(KnownHeaderName.HOST, "yourdomain.com"));
		
		server.processHttpRequests(mockResponseSocket, req, false);
		
		List<HttpPayload> responses = mockResponseSocket.getResponses();
		Assert.assertEquals(1, responses.size());

		HttpPayload httpPayload = responses.get(0);
		HttpResponse httpResponse = httpPayload.getHttpResponse();
		Assert.assertEquals(KnownStatusCode.HTTP_303_SEEOTHER, httpResponse.getStatusLine().getStatus().getKnownStatus());
	}
	
	@Test
	public void testAsyncControllerAndRemoteSystem() {
		
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			//ie.
			//binder.bind(SomeRemoteSystem.class).toInstance(mockRemote); //see above comment on the field mockRemote
		}
	}
	
}
