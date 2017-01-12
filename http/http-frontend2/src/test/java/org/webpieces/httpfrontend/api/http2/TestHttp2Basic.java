package org.webpieces.httpfrontend.api.http2;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketDataWriter;
import org.webpieces.httpfrontend.api.MockTimer;
import org.webpieces.httpfrontend.api.adaptor.AdaptorChannelManager;
import org.webpieces.httpfrontend.api.http2.mock.MockHttpRequestListener;
import org.webpieces.httpfrontend.api.http2.mock.MockResponseListener;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class TestHttp2Basic {

	private MockClientListener mockClientListener = new MockClientListener();
	private Http2Client client;
	private InetSocketAddress svrAddress;

	@Before
	public void setUp() {
		MockHttpRequestListener mockRequestListener = new MockHttpRequestListener();
		
		svrAddress = new InetSocketAddress(8888);
        AdaptorChannelManager adaptor = new AdaptorChannelManager();

        MockTimer timer = new MockTimer();
        BufferCreationPool pool = new BufferCreationPool();

        AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(adaptor);
        HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, pool);
        FrontendConfig config = new FrontendConfig("httpFrontend", svrAddress);
        HttpServer server = mgr.createHttpServer(config, mockRequestListener);
        server.start();

        client = Http2ClientFactory.createHttpClient(adaptor);
	}
	
	@Test
	public void testBasicIntegration() throws InterruptedException, ExecutionException {
		Http2Socket socket = client.createHttpSocket("simple");
		
		CompletableFuture<Http2Socket> connect = socket.connect(svrAddress, mockClientListener);
		
		Http2Socket socket2 = connect.get();

		Http2Headers request = createRequest();
		
		MockResponseListener listener1 = new MockResponseListener();
		MockResponseListener listener2 = new MockResponseListener();
		CompletableFuture<Http2SocketDataWriter> future = socket2.sendRequest(request, listener1);
		CompletableFuture<Http2SocketDataWriter> future2 = socket2.sendRequest(request, listener2);
		
		
		
	}

	private Http2Headers createRequest() {
    	List<Http2Header> headers = new ArrayList<>();
    	
        headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
        headers.add(new Http2Header(Http2HeaderName.AUTHORITY, "somehost.com"));
        headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
        headers.add(new Http2Header(Http2HeaderName.SCHEME, "http"));
        headers.add(new Http2Header(Http2HeaderName.HOST, "somehost.com"));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
        
        Http2Headers request = new Http2Headers(headers);
		return request;
	}
}
