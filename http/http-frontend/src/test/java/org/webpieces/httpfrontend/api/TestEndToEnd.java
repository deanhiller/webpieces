package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

public class TestEndToEnd {
  private int serverPort;
  private HttpClientSocket socket;

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
    ConcurrentHashMap<ResponseId, List<Object>> responses = mockResponseListener.getResponseLog(1000, 2);

    // We got two responses (one regular and one push)
    Assert.assertEquals(responses.size(), 2);

    // The first response should have a 200
    List<Object> firstResponse = responses.get(new ResponseId(1));
    Assert.assertTrue(HttpResponse.class.isInstance(firstResponse.get(0)));
    Assert.assertEquals(((HttpResponse) firstResponse.get(0)).getStatusLine().getStatus().getCode().intValue(), 200);

    // The data should have the main response
    Assert.assertTrue(DataWrapper.class.isInstance(firstResponse.get(1)));
    Assert.assertArrayEquals(((DataWrapper) firstResponse.get(1)).createByteArray(), ServerFactory.MAIN_RESPONSE.getBytes());

    // The second response should be the pushed response
    List<Object> secondResponse = responses.get(new ResponseId(2));
    Assert.assertTrue(HttpResponse.class.isInstance(secondResponse.get(0)));
    Assert.assertEquals(((HttpResponse) secondResponse.get(0)).getStatusLine().getStatus().getCode().intValue(), 200);

    // The data should have the main response
    Assert.assertTrue(DataWrapper.class.isInstance(secondResponse.get(1)));
    Assert.assertArrayEquals(((DataWrapper) secondResponse.get(1)).createByteArray(), ServerFactory.PUSHED_RESPONSE.getBytes());

  }
}
