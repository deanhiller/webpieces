package org.webpieces.httpfrontend.api;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpcommon.api.Http2FullHeaders;
import org.webpieces.httpcommon.api.Http2FullPushPromise;
import org.webpieces.httpcommon.temp.TempHttp2Parser;
import org.webpieces.httpcommon.temp.TempHttp2ParserFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.handlers.DataListener;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class TestRequestResponse {

    private HttpParser httpParser;
    private TempHttp2Parser http2Parser;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private SettingsFrame settingsFrame = new SettingsFrame();
    private Decoder decoder;
    private List<Http2Setting> settings = new ArrayList<>();
    private static String blahblah = "blah blah blah";

    @Before
    public void setup() {
        BufferCreationPool pool = new BufferCreationPool();

        httpParser = HttpParserFactory.createParser(pool);
        http2Parser = TempHttp2ParserFactory.createParser(pool);
        decoder = new Decoder(4096, 4096);
        settings.add(new Http2Setting(SettingsParameter.SETTINGS_MAX_FRAME_SIZE, 16384L));
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

    private void simpleHttp11RequestWithListener(RequestListenerForTest listener) throws InterruptedException, ExecutionException {
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        ByteBuffer bytesWritten = processRequestWithRequestListener(request, listener);
        Assert.assertTrue(new String(bytesWritten.array()).contains("HTTP/1.1 200 OK\r\n" +
            "Content-Length: 0\r\n"));
    }

    private void upgradeHttp2RequestWithListener(RequestListenerForTest listener) throws InterruptedException, ExecutionException {
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

        // strip the header
        byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
            Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, listener);

        Memento memento = httpParser.prepareToParse();
        httpParser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        ParserResult parse2State = http2Parser.prepareToParse();
        // Check that we got a settings frame, a headers frame, and a data frame
        ParserResult result = http2Parser.parse(parse2State, leftOverData, decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(3, frames.size());
        Assert.assertTrue(SettingsFrame.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2FullHeaders.class.isInstance(frames.get(1)));
        Assert.assertTrue(DataFrame.class.isInstance(frames.get(2)));
    }

    private void http2ResponseWithData(RequestListenerForTest listener) throws InterruptedException, ExecutionException {
        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

        // strip the header
        byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
            Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, listener);

        Memento memento = httpParser.prepareToParse();
        httpParser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        ParserResult parse2State = http2Parser.prepareToParse();
        // Check that we got a settings frame, a headers frame, and a data frame
        ParserResult result = http2Parser.parse(parse2State, leftOverData, decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(3, frames.size());
        Assert.assertTrue(SettingsFrame.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2FullHeaders.class.isInstance(frames.get(1)));
        Assert.assertTrue(DataFrame.class.isInstance(frames.get(2)));

        Assert.assertArrayEquals(((DataFrame) frames.get(2)).getData().createByteArray(), blahblah.getBytes());
    }

    private void http2WithPushPromise(RequestListenerForTest listener) throws InterruptedException, ExecutionException {


        HttpRequest request = Requests.createRequest(KnownHttpMethod.GET, "/");
        request.addHeader(new Header(KnownHeaderName.UPGRADE, "h2c"));
        request.addHeader(new Header(KnownHeaderName.CONNECTION, "Upgrade, HTTP2-Settings"));
        byte[] settingsFrameBytes = http2Parser.marshal(settingsFrame).createByteArray();

        // strip the header
        byte[] settingsFramePayload = Arrays.copyOfRange(settingsFrameBytes, 9, settingsFrameBytes.length);
        request.addHeader(new Header(KnownHeaderName.HTTP2_SETTINGS,
            Base64.getUrlEncoder().encodeToString(settingsFramePayload) + " "));

        ByteBuffer bytesWritten = processRequestWithRequestListener(request, listener);

        Memento memento = httpParser.prepareToParse();
        httpParser.parse(memento, dataGen.wrapByteBuffer(bytesWritten));
        List<HttpPayload> parsedMessages = memento.getParsedMessages();
        DataWrapper leftOverData = memento.getLeftOverData();

        // Check that we got an approved upgrade
        Assert.assertEquals(parsedMessages.size(), 1);
        Assert.assertTrue(HttpResponse.class.isInstance(parsedMessages.get(0)));
        HttpResponse responseGot = (HttpResponse) parsedMessages.get(0);
        Assert.assertEquals(responseGot.getStatusLine().getStatus().getKnownStatus(), KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);

        ParserResult parse2State = http2Parser.prepareToParse();
        // Check that we got a settings frame, a headers frame, and a data frame, then a push promise frame
        // then a headers then a data frame
        ParserResult result = http2Parser.parse(parse2State, leftOverData, decoder, settings);
        List<Http2Frame> frames = result.getParsedFrames();

        Assert.assertEquals(6, frames.size());
        Assert.assertTrue(SettingsFrame.class.isInstance(frames.get(0)));
        Assert.assertTrue(Http2FullHeaders.class.isInstance(frames.get(1)));
        Assert.assertTrue(DataFrame.class.isInstance(frames.get(2)));
        Assert.assertTrue(Http2FullPushPromise.class.isInstance(frames.get(3)));
        Assert.assertTrue(Http2FullHeaders.class.isInstance(frames.get(4)));
        Assert.assertTrue(DataFrame.class.isInstance(frames.get(5)));
    }

    @Test
    public void testSimpleHttp11Request() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());
        RequestListenerForTest listenerChunked = new RequestListenerForTestWithResponses(response, true);
        simpleHttp11RequestWithListener(listenerChunked);

        RequestListenerForTest listenerNotChunked = new RequestListenerForTestWithResponses(response, false);
        simpleHttp11RequestWithListener(listenerNotChunked);
    }

    @Test
    public void testUpgradeHttp2Request() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());
        RequestListenerForTest listenerChunked = new RequestListenerForTestWithResponses(response, true);
        upgradeHttp2RequestWithListener(listenerChunked);

        RequestListenerForTest listenerNotChunked = new RequestListenerForTestWithResponses(response, false);
        upgradeHttp2RequestWithListener(listenerNotChunked);
    }

    @Test
    public void testHttp2ResponseWithData() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapByteArray(blahblah.getBytes()));
        RequestListenerForTest listenerChunked = new RequestListenerForTestWithResponses(response, true);
        http2ResponseWithData(listenerChunked);

        RequestListenerForTest listenerNotChunked = new RequestListenerForTestWithResponses(response, false);
        http2ResponseWithData(listenerNotChunked);
    }

    @Test
    public void testHttp2WithPushPromiseResponses() throws InterruptedException, ExecutionException {
        HttpResponse response = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapByteArray(blahblah.getBytes()));
        List<HttpResponse> responses = new ArrayList<>();
        responses.add(response);
        responses.add(response);

        RequestListenerForTest listenerChunked = new RequestListenerForTestWithResponses(responses, true);
        http2WithPushPromise(listenerChunked);

        RequestListenerForTest listenerNotChunked = new RequestListenerForTestWithResponses(responses, false);
        http2WithPushPromise(listenerNotChunked);
    }


}
