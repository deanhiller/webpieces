package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class MockServer {
    private MockTcpChannel mockTcpChannel = new MockTcpChannel();
    private DataListener dataListener;

    public MockServer(
            int port,
            boolean alwaysHttp2,
            RequestListenerForTest requestListenerForTest)
            throws InterruptedException, ExecutionException
    {
        MockTcpServerChannel mockTcpServerChannel = new MockTcpServerChannel();
        MockChannelManager mockChanMgr = new MockChannelManager();
        MockTimer timer = new MockTimer();
        BufferCreationPool pool = new BufferCreationPool();

        mockChanMgr.addTcpSvrChannel(mockTcpServerChannel);
        AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(mockChanMgr);
        HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, pool);

        FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(port));
        config.maxConnectToRequestTimeoutMs = 5000;
        config.alwaysHttp2 = alwaysHttp2;

        HttpServer server = mgr.createHttpServer(config, requestListenerForTest);
        server.start();

        ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
        Assert.assertEquals(1, listeners.length);

        MockFuture<?> mockFuture = new MockFuture<>();
        timer.addMockFuture(mockFuture);
        ConnectionListener listener = listeners[0];
        CompletableFuture<DataListener> future = listener.connected(mockTcpChannel, true);

        dataListener = future.get();
    }

    DataListener getDataListener() {
        return dataListener;
    }

    MockTcpChannel getMockTcpChannel() {
        return mockTcpChannel;
    }
}
