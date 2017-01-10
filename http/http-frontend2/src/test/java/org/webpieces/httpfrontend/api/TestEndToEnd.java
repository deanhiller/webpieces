package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.Http2SettingsMap;
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

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class TestEndToEnd {
  private int serverPort;
  private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
  private Http2SettingsMap basicClientSettings;

  private static HttpClient createHttpClient(Http2SettingsMap http2SettingsMap) {
    BufferPool pool2 = new BufferCreationPool();
    Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
    ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
    ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor2);

    HttpParser httpParser = HttpParserFactory.createParser(pool2);
    HpackParser http2Parser = HpackParserFactory.createParser(pool2, true);

    return HttpClientFactory.createHttpClient(mgr, httpParser, http2Parser, http2SettingsMap);
  }

  @Before
  public void setUp() {
    serverPort = ServerFactory.createTestServer(false, 100L);
    basicClientSettings = new Http2SettingsMap();
    basicClientSettings.put(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, 100L);
  }

  @Ignore
  @Test
  public void testSimpleRequest() throws InterruptedException, ExecutionException {
    HttpClient client = createHttpClient(basicClientSettings);
    HttpClientSocket socket = client.openHttpSocket("testClient");

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

  @Ignore
  @Test
  public void testMultipleRequests() throws InterruptedException, ExecutionException {
    HttpClient client = createHttpClient(basicClientSettings);
    HttpClientSocket socket = client.openHttpSocket("testClient");

    InetSocketAddress addr = new InetSocketAddress("localhost", serverPort);
    RequestSender requestSender = socket.connect(addr).get();
    HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
    MockResponseListener mockResponseListener = new MockResponseListener();

    requestSender.sendRequest(request, true, mockResponseListener);
    ConcurrentHashMap<ResponseId, List<Object>> responses = mockResponseListener.getResponseLog(1000, 2);

    // We got two responses (one regular and one push)
    Assert.assertEquals(responses.size(), 2);

    // Now make a bunch of requests
    mockResponseListener.clear();

    // Have to make a new request because the client munges the request when making the upgrade
    // TODO: fix that
    HttpRequest cleanRequest = Requests.createRequest(KnownHttpMethod.GET, "/");

    requestSender.sendRequest(cleanRequest, true, mockResponseListener);
    requestSender.sendRequest(cleanRequest, true, mockResponseListener);
    requestSender.sendRequest(cleanRequest, true, mockResponseListener);
    requestSender.sendRequest(cleanRequest, true, mockResponseListener);

    // Should get 8 responses
    ConcurrentHashMap<ResponseId, List<Object>> responsesTwo = mockResponseListener.getResponseLog(1000, 8);
    Assert.assertEquals(responsesTwo.size(), 8);

  }

  private DataWrapper chainDataWrappers(List<DataWrapper> listOfWrappers) {
    if(listOfWrappers.size() == 0) {
      return dataGen.emptyWrapper();
    }
    else {
      DataWrapper head = listOfWrappers.remove(0);

      return dataGen.chainDataWrappers(head, chainDataWrappers(listOfWrappers));
    }
  }

  @Ignore
  @Test
  public void testRequestWithWindowSizeOne() throws InterruptedException, ExecutionException {
    Http2SettingsMap http2SettingsMap = new Http2SettingsMap();
    http2SettingsMap.put(SettingsParameter.SETTINGS_INITIAL_WINDOW_SIZE, 1L);

    HttpClient client = createHttpClient(http2SettingsMap);
    HttpClientSocket socket = client.openHttpSocket("testClient");

    InetSocketAddress addr = new InetSocketAddress("localhost", serverPort);
    RequestSender requestSender = socket.connect(addr).get();
    HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");

    // Create a response listener that delays dealing with data.
    MockResponseListener mockResponseListener = new MockResponseListener(500);

    requestSender.sendRequest(request, true, mockResponseListener);
    ConcurrentHashMap<ResponseId, List<Object>> responses = mockResponseListener.getResponseLog(10000, 2);

    // We got two responses (one regular and one push)
    Assert.assertEquals(responses.size(), 2);

    // The first response should have a 200
    List<Object> firstResponse = responses.get(new ResponseId(1));
    Assert.assertTrue(HttpResponse.class.isInstance(firstResponse.get(0)));
    Assert.assertEquals(((HttpResponse) firstResponse.get(0)).getStatusLine().getStatus().getCode().intValue(), 200);

    // Assemble the data
    firstResponse.remove(0);
    DataWrapper firstResponseData = chainDataWrappers(firstResponse.stream()
        .map(x -> (DataWrapper) x)
        .collect(Collectors.toList()));
    Assert.assertArrayEquals(firstResponseData.createByteArray(), ServerFactory.MAIN_RESPONSE.getBytes());

    // Make sure all the data in the firstresponse has size 1
    firstResponse.stream()
        .map(x -> (DataWrapper) x)
        .forEach(dw -> Assert.assertEquals(dw.getReadableSize(), 1));

    // The second response should be the pushed response
    List<Object> secondResponse = responses.get(new ResponseId(2));
    Assert.assertTrue(HttpResponse.class.isInstance(secondResponse.get(0)));
    Assert.assertEquals(((HttpResponse) secondResponse.get(0)).getStatusLine().getStatus().getCode().intValue(), 200);

    secondResponse.remove(0);
    DataWrapper secondResponseData = chainDataWrappers(secondResponse.stream()
        .map(x -> (DataWrapper) x)
        .collect(Collectors.toList()));
    Assert.assertArrayEquals(secondResponseData.createByteArray(), ServerFactory.PUSHED_RESPONSE.getBytes());

    // Make sure all the data in the second response has size 1
    secondResponse.stream()
        .map(x -> (DataWrapper) x)
        .forEach(dw -> Assert.assertEquals(dw.getReadableSize(), 1));
  }

  // This test is flaky because the client sometimes loses the server settings that
  // get sent as soon as the upgrade has gone through.

  /*

A success looks like this:

16:03:13.380 [frontEnd2] INFO org.webpieces.frontend.api.HttpServerSocket - Sending local requested settings
16:03:13.380 [frontEnd2] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - sending settings: Http2Settings{ack=false, settings={SETTINGS_MAX_CONCURRENT_STREAMS=100, SETTINGS_MAX_HEADER_LIST_SIZE=4096, SETTINGS_MAX_FRAME_SIZE=16921}} Http2Frame{streamId=0}
16:03:13.386 [clientThread2] INFO org.webpieces.httpclient.impl.RequestSenderImpl - http11 incomingData -> size=71
16:03:13.387 [clientThread2] INFO org.webpieces.httpclient.impl.RequestSenderImpl - upgrade succeeded
16:03:13.387 [clientThread2] INFO org.webpieces.httpcommon.impl.Http2ServerEngineImpl - sending preface
16:03:13.387 [clientThread2] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - sending settings: Http2Settings{ack=false, settings={SETTINGS_ENABLE_PUSH=0}} Http2Frame{streamId=0}

A failure looks like this:

10:12:12.573 [frontEnd1] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - got http2 upgrade with settings: 000200000000
10:12:12.576 [frontEnd1] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - Setting remote settings to: {SETTINGS_ENABLE_PUSH=0}
10:12:12.581 [frontEnd2] INFO org.webpieces.frontend.api.HttpServerSocket - Sending local requested settings
10:12:12.581 [frontEnd2] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - sending settings: Http2Settings{ack=false, settings={SETTINGS_MAX_FRAME_SIZE=16921, SETTINGS_MAX_CONCURRENT_STREAMS=100, SETTINGS_MAX_HEADER_LIST_SIZE=4096}} Http2Frame{streamId=0}
10:12:12.582 [clientThread2] INFO org.webpieces.httpclient.impl.RequestSenderImpl - http11 incomingData -> size=71
10:12:12.583 [clientThread3] INFO org.webpieces.httpclient.impl.RequestSenderImpl - http11 incomingData -> size=27
10:12:12.583 [frontEnd2] INFO org.webpieces.httpcommon.impl.Stream - 1: IDLE -> HALF_CLOSED_REMOTE
10:12:12.584 [main] INFO org.webpieces.httpclient.impl.RequestSenderImpl - upgrade succeeded
10:12:12.588 [main] INFO org.webpieces.httpcommon.impl.Http2ServerEngineImpl - sending preface
10:12:12.588 [main] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - sending settings: Http2Settings{ack=false, settings={SETTINGS_ENABLE_PUSH=0}} Http2Frame{streamId=0}
10:12:12.590 [main] INFO org.webpieces.httpcommon.impl.Stream - 1: IDLE -> HALF_CLOSED_LOCAL
10:12:12.590 [main] INFO org.webpieces.httpcommon.impl.Http2ServerEngineImpl - got leftover data that we're passing on to the http2 parser
10:12:12.597 [frontEnd2] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - sending header frames: [Http2Headers{endStream=false, endHeaders=true, priority=false, priorityDetails=PriorityDetails{streamDependencyIsExclusive=false, streamDependency=0, weight=0}, headerFragment=org.webpieces.data.impl.ByteBufferDataWrapper@409529e8, headerList=null, padding=com.webpieces.http2parser.impl.PaddingImpl@26e0011d} Http2Frame{streamId=1}]
10:12:12.601 [frontEnd2] INFO org.webpieces.httpcommon.impl.Http2ServerEngineImpl - push promise not permitted by client, ignoring pushed response
10:12:12.602 [frontEnd4] INFO org.webpieces.httpcommon.impl.Http2EngineImpl - got http2 preface

Because it seems that the settings frame that the server is sending back is going into thte http11 parser not the
http2 parser. The peeking into the http11 parser's leftoverdata and the request body should be able
to deal with that, but apparently it doesn't... it does appear to get leftover data but that leftoverdata
doesn't get turned into a settings frame.

This might be fixed. The problem I think was that the thread processing the settings frame hadn't gotten
around to it when the headers frame came in, so I added a thing so that if the settings frame hasn't
been processed we wait 500ms for the settings frame to be processed.

*/

  @Ignore
  @Test
  public void testRequestNoPush() throws InterruptedException, ExecutionException  {
    Http2SettingsMap http2SettingsMap = new Http2SettingsMap();
    http2SettingsMap.put(SettingsParameter.SETTINGS_ENABLE_PUSH, 0L);

    HttpClient client = createHttpClient(http2SettingsMap);
    HttpClientSocket socket = client.openHttpSocket("testClient");

    InetSocketAddress addr = new InetSocketAddress("localhost", serverPort);
    RequestSender requestSender = socket.connect(addr).get();
    HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
    MockResponseListener mockResponseListener = new MockResponseListener();

    requestSender.sendRequest(request, true, mockResponseListener);
    ConcurrentHashMap<ResponseId, List<Object>> responses = mockResponseListener.getResponseLog(1000, 1);

    // We get only one response because push is disabled
    Assert.assertEquals(1, responses.size());

  }

  @Ignore
  @Test
  public void testRequestOneStream() throws InterruptedException, ExecutionException  {
    Http2SettingsMap http2SettingsMap = new Http2SettingsMap();
    http2SettingsMap.put(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, 0L);

    HttpClient client = createHttpClient(http2SettingsMap);
    HttpClientSocket socket = client.openHttpSocket("testClient");

    InetSocketAddress addr = new InetSocketAddress("localhost", serverPort);
    RequestSender requestSender = socket.connect(addr).get();
    HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
    MockResponseListener mockResponseListener = new MockResponseListener();

    requestSender.sendRequest(request, true, mockResponseListener);
    ConcurrentHashMap<ResponseId, List<Object>> responses = mockResponseListener.getResponseLog(1000, 1);

    // We get only one response because only one stream is allowed
    Assert.assertEquals(1, responses.size());

  }
}
