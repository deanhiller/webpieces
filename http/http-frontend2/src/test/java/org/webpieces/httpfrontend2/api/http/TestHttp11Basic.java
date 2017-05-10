package org.webpieces.httpfrontend2.api.http;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.httpfrontend2.api.Responses;
import org.webpieces.httpfrontend2.api.http2.mock.MockHttpRequestListener;
import org.webpieces.httpfrontend2.api.http2.mock.RequestData;
import org.webpieces.httpfrontend2.api.mock.MockChannelManager;
import org.webpieces.httpfrontend2.api.mock.MockHttp11Channel;
import org.webpieces.httpfrontend2.api.mock2.MockTcpServerChannel;
import org.webpieces.httpfrontend2.api.mock2.MockTimer;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpMessage;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;


public class TestHttp11Basic {

	private InetSocketAddress svrAddress;
	private ConnectionListener connListener;
	private MockHttp11Channel mockTcpChannel;
	private MockHttpRequestListener mockRequestListener;

	@Before
	public void setUp() throws InterruptedException, ExecutionException {
		mockRequestListener = new MockHttpRequestListener();
		
		svrAddress = new InetSocketAddress(8888);
        MockChannelManager adaptor = new MockChannelManager();
        adaptor.addTcpSvrChannel(new MockTcpServerChannel());

        MockTimer timer = new MockTimer();
        BufferCreationPool pool = new BufferCreationPool();

        AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(adaptor);
        HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, pool);
        FrontendConfig config = new FrontendConfig("httpFrontend", svrAddress);
        HttpServer server = mgr.createHttpServer(config, mockRequestListener);
        server.start();

        connListener = adaptor.getConnListener();
        
        mockTcpChannel = new MockHttp11Channel();
        CompletableFuture<DataListener> future = connListener.connected(mockTcpChannel, true);
        mockTcpChannel.setDataListener(future.get());
	}
	
	@Ignore
	@Test
	public void testSendTwoRequestsAndMisorderedResponses() throws InterruptedException, ExecutionException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		
		mockTcpChannel.writeHttpMsg(req);
		RequestData data1 = mockRequestListener.getRequestDataAndClear();
		mockTcpChannel.writeHttpMsg(req2);
		RequestData data2 = mockRequestListener.getRequestDataAndClear();
		
		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
		data2.getStream().sendResponse(Responses.createResponse(2));

		//assert NOT received
		mockTcpChannel.assertNoMessages();
		
		data1.getStream().sendResponse(Responses.createResponse(1));
		
		List<HttpMessage> msgs = mockTcpChannel.getHttpResponsesAndClear();
		Assert.assertEquals(2,  msgs);
		
		HttpResponse resp1 = (HttpResponse) msgs.get(0);
		HttpResponse resp2 = (HttpResponse) msgs.get(1);
		
		Header header = resp1.getHeaderLookupStruct().getHeader(KnownHeaderName.SERVER);
		Assert.assertEquals("1", header.getValue());
		
		Header header2 = resp2.getHeaderLookupStruct().getHeader(KnownHeaderName.SERVER);
		Assert.assertEquals("2", header2.getValue());
	}


}
