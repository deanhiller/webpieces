package org.webpieces.httpfrontend.api;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestRequestResponse {
    private MockTcpChannel mockServerChannel = new MockTcpChannel();
    private MockTcpServerChannel mockChannel = new MockTcpServerChannel();
    private MockChannelManager mockChanMgr = new MockChannelManager();
    private MockTimer timer = new MockTimer();
    private HttpParser parser;
    private Http2Parser http2Parser;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private Http2Settings settingsFrame = new Http2Settings();
    private Decoder decoder;
    Map<Http2Settings.Parameter, Integer> settings = new HashMap<>();

    private HttpFrontendManager mgr;

    @Before
    public void setup() {
        mockChanMgr.addTcpSvrChannel(mockChannel);
        AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(mockChanMgr);
        BufferCreationPool pool = new BufferCreationPool();
        mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, pool);
        parser = HttpParserFactory.createParser(pool);
        http2Parser = Http2ParserFactory.createParser(pool);
        decoder = new Decoder(4096, 4096);
        settings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 16384);
    }

    private ByteBuffer processRequestWithRequestListener(HttpRequest request, MockRequestListener mockRequestListener) throws InterruptedException, ExecutionException {
        FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
        config.maxConnectToRequestTimeoutMs = 5000;

        mgr.createHttpServer(config , mockRequestListener);

        ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
        Assert.assertEquals(1, listeners.length);

        MockFuture<?> mockFuture = new MockFuture<>();
        timer.addMockFuture(mockFuture);
        ConnectionListener listener = listeners[0];
        CompletableFuture<DataListener> future = listener.connected(mockServerChannel, true);

        DataListener dataListener = future.get();

        ByteBuffer buffer = parser.marshalToByteBuffer(request);
        dataListener.incomingData(mockServerChannel, buffer);

        Thread.sleep(1000);

        return mockServerChannel.getWriteLog();
    }

    @Test
    public void testSimpleHttp11Request() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new MockRequestListenerWithResponses(response));
        Assert.assertEquals("HTTP/1.1 200 OK\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n", new String(bytesWritten.array()));
    }

    @Test
    public void testUpgradeHttp2Request() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                Base64.getUrlEncoder().encodeToString(http2Parser.marshal(settingsFrame).createByteArray()) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new MockRequestListenerWithResponses(response));

        Memento memento = parser.prepareToParse();
        parser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        // Check that we got a settings frame, a settings ack frame, a headers frame, and a data frame
        ParserResult result = http2Parser.parse(leftOverData, dataGen.emptyWrapper(), decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(frames.size(), 4);
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(1)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(2)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(3)));
    }

    @Test
    public void testHttp2ResponseWithData() throws InterruptedException, ExecutionException {
        String blahblah = "blah blah blah";
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapByteArray(blahblah.getBytes()));
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                Base64.getUrlEncoder().encodeToString(http2Parser.marshal(settingsFrame).createByteArray()) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new MockRequestListenerWithResponses(response));

        Memento memento = parser.prepareToParse();
        parser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        // Check that we got a settings frame, a settings ack frame, a headers frame, and a data frame
        ParserResult result = http2Parser.parse(leftOverData, dataGen.emptyWrapper(), decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(frames.size(), 4);
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(1)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(2)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(3)));

        Assert.assertArrayEquals(((Http2Data) frames.get(3)).getData().createByteArray(), blahblah.getBytes());
    }

    @Test
    public void testHttp2WithPushPromiseResponses() throws InterruptedException, ExecutionException {
        String blahblah = "blah blah blah";
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapByteArray(blahblah.getBytes()));
        List<HttpResponse> responses = new ArrayList<>();
        responses.add(response);
        responses.add(response);

        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                Base64.getUrlEncoder().encodeToString(http2Parser.marshal(settingsFrame).createByteArray()) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new MockRequestListenerWithResponses(responses));

        Memento memento = parser.prepareToParse();
        parser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        // Check that we got a settings frame, a settings ack frame, a headers frame, and a data frame, then a push promise frame
        // then a headers then a data frame
        ParserResult result = http2Parser.parse(leftOverData, dataGen.emptyWrapper(), decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(frames.size(), 7);
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(1)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(2)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(3)));
        Assert.assertTrue(Http2PushPromise.class.isInstance(frames.get(4)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(5)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(6)));
    }


}
