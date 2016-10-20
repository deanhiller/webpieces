package org.webpieces.httpfrontend.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestRequestResponse {
    private MockTcpChannel mockServerChannel = new MockTcpChannel();
    private MockTcpServerChannel mockChannel = new MockTcpServerChannel();
    private MockChannelManager mockChanMgr = new MockChannelManager();
    private MockTimer timer = new MockTimer();
    private HttpParser parser;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();


    private HttpFrontendManager mgr;

    @Before
    public void setup() {
        mockChanMgr.addTcpSvrChannel(mockChannel);
        AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(mockChanMgr);
        BufferCreationPool pool = new BufferCreationPool();
        mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, pool);
        parser = HttpParserFactory.createParser(pool);
    }

    @Test
    public void testSimpleHttp11Request() throws InterruptedException, ExecutionException {
        FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
        config.maxConnectToRequestTimeoutMs = 5000;

        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());
        MockRequestListener mockRequestListener = new MockRequestListenerWithResponse(response);
        mgr.createHttpServer(config , mockRequestListener);

        ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
        Assert.assertEquals(1, listeners.length);

        MockFuture<?> mockFuture = new MockFuture<>();
        timer.addMockFuture(mockFuture);
        ConnectionListener listener = listeners[0];
        CompletableFuture<DataListener> future = listener.connected(mockServerChannel, true);

        DataListener dataListener = future.get();

        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        ByteBuffer buffer = parser.marshalToByteBuffer(request);
        dataListener.incomingData(mockServerChannel, buffer);

        ByteBuffer writeLog = mockServerChannel.getWriteLog();
        Assert.assertEquals("HTTP/1.1 200 OK\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n", new String(writeLog.array()));
    }


}
