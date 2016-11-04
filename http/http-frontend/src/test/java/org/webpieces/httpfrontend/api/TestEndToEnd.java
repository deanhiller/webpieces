package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

public class TestEndToEnd {
  int serverPort;
  HttpClientSocket socket;

  private static HttpClient createHttpClient() {
    BufferPool pool2 = new BufferCreationPool();
    Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
    ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
    ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor2);

    HttpParser httpParser = HttpParserFactory.createParser(pool2);
    Http2Parser http2Parser = Http2ParserFactory.createParser(pool2);

    return HttpClientFactory.createHttpClient(mgr, httpParser, http2Parser);
  }

  @Before
  public void setUp() {
    serverPort = ServerFactory.createTestServer(false);
    HttpClient client = createHttpClient();
    socket = client.openHttpSocket("testClient");
  }

  @Test
  public void testSimpleRequest() throws InterruptedException, ExecutionException {
    InetSocketAddress addr = new InetSocketAddress("localhost", serverPort);
    RequestSender requestSender = socket.connect(addr).get();
    HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
    MockResponseListener mockResponseListener = new MockResponseListener();

    requestSender.sendRequest(request, true, mockResponseListener);
  }
}
