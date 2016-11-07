package org.webpieces.httpfrontend.api;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.handlers.DataListener;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class TestRequestResponse {

    private HttpParser httpParser;
    private Http2Parser http2Parser;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private Http2Settings settingsFrame = new Http2Settings();
    private Decoder decoder;
    private Http2SettingsMap settings = new Http2SettingsMap();

    @Before
    public void setup() {
        BufferCreationPool pool = new BufferCreationPool();

        httpParser = HttpParserFactory.createParser(pool);
        http2Parser = Http2ParserFactory.createParser(pool);
        decoder = new Decoder(4096, 4096);
        settings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 16384L);
        settingsFrame.setSettings(settings);
    }

    private ByteBuffer processRequestWithRequestListener(HttpRequest request, RequestListenerForTest requestListenerForTest)
            throws InterruptedException, ExecutionException {
        MockServer mockServer = new MockServer(80, false, requestListenerForTest);
        DataListener dataListener = mockServer.getDataListener();

        ByteBuffer buffer = httpParser.marshalToByteBuffer(request);
        dataListener.incomingData(mockServer.getMockTcpChannel(), buffer);

        // TODO: fix this to wait until we're done, not just sleep, which is fragile.
        Thread.sleep(1000);

        return mockServer.getMockTcpChannel().getWriteLog();
    }

    @Test
    public void testSimpleHttp11Request() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new RequestListenerForTestWithResponses(response));
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
        byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

        // strip the header
        byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new RequestListenerForTestWithResponses(response));

        Memento memento = httpParser.prepareToParse();
        httpParser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        // Check that we got a settings frame, a headers frame, and a data frame
        ParserResult result = http2Parser.parse(leftOverData, dataGen.emptyWrapper(), decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(3, frames.size());
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(1)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(2)));
    }

    @Test
    public void testHttp2ResponseWithData() throws InterruptedException, ExecutionException {
        String blahblah = "blah blah blah";
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapByteArray(blahblah.getBytes()));
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

        // strip the header
        byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new RequestListenerForTestWithResponses(response));

        Memento memento = httpParser.prepareToParse();
        httpParser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        // Check that we got a settings frame, a headers frame, and a data frame
        ParserResult result = http2Parser.parse(leftOverData, dataGen.emptyWrapper(), decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(3, frames.size());
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(1)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(2)));

        Assert.assertArrayEquals(((Http2Data) frames.get(2)).getData().createByteArray(), blahblah.getBytes());
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
        byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

        // strip the header
        byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
                Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, new RequestListenerForTestWithResponses(responses));

        Memento memento = httpParser.prepareToParse();
        httpParser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        // Check that we got a settings frame, a headers frame, and a data frame, then a push promise frame
        // then a headers then a data frame
        ParserResult result = http2Parser.parse(leftOverData, dataGen.emptyWrapper(), decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(6, frames.size());
        Assert.assertTrue(Http2Settings.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(1)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(2)));
        Assert.assertTrue(Http2PushPromise.class.isInstance(frames.get(3)));
        Assert.assertTrue(Http2Headers.class.isInstance(frames.get(4)));
        Assert.assertTrue(Http2Data.class.isInstance(frames.get(5)));
    }


}
