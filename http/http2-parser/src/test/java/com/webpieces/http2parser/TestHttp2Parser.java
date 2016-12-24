package com.webpieces.http2parser;

import static com.webpieces.http2parser.api.dto.Http2FrameType.HEADERS;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2FrameType;
import com.webpieces.http2parser.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import com.webpieces.http2parser.api.dto.Http2Settings;

public class TestHttp2Parser {
    private static String aBunchOfDataFrames =
            "00 00 08" + // Length
            "00" + // Type
            "00" + // Flags
            "00 00 00 01" + // R + streamid
            "FF FF FF FF FF FF FF FF" + // payload
            "00 00 08" + // Length
            "00" + // Type
            "01" + // Flags - endStream = true
            "00 00 00 01" + // R + streamid
            "FF FF FF FF FF FF FF FF" + // payload
            "00 00 0B" + // Length
            "00" + // Type
            "08" + // Flags (padded = true)
            "00 00 00 01" + // R + streamid
            "02" + // padding length
            "FF FF FF FF FF FF FF FF" + // data
            "00 00" + // padding
            "00 00 0B" + // Length
            "00" + // Type
            "09" + // Flags - endStream = true
            "00 00 00 01" + // R + streamid
            "02" + // padding length
            "FF FF FF FF FF FF FF FF" + // payload
            "00 00"; // padding
    private static String dataFramesWithSomeLeftOverData = aBunchOfDataFrames +
            "00 00";

    private static String dataFramesWithABunchOfLeftOverData = aBunchOfDataFrames +
            "00 00 08" + // length
            "00"; // type

    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private static Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());
    private static Http2Parser2 parser2 = Http2ParserFactory.createParser2(new BufferCreationPool());

    private static LinkedList<HasHeaderFragment.Header> basicRequestHeaders = new LinkedList<>();
    private static LinkedList<HasHeaderFragment.Header> basicResponseHeaders = new LinkedList<>();
    private static LinkedList<HasHeaderFragment.Header> ngHttp2ExampleHeaders = new LinkedList<>();

    private Decoder decoder = new Decoder(4096, 4096);
    private Encoder encoder = new Encoder(4096);
    private static Http2SettingsMap settings = new Http2SettingsMap();

    // https://github.com/http2jp/hpack-test-case/blob/master/nghttp2/story_00.json
    private static String basicRequestSerializationNghttp2 = "82864188f439ce75c875fa5784";

    private static String ngHttp2ExampleHeaderFragment =
            "82 84 86 41 88 aa 69 d2 9a c4 b9 ec 9b 53 03 2a 2f" +
            "2a 90 7a 8a aa 69 d2 9a c4 c0 57 0b 6b 83";
    static {
        basicRequestHeaders.add(new HasHeaderFragment.Header(":method", "GET"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":scheme", "http"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":authority", "yahoo.co.jp"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":path", "/"));

        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header(":method", "GET"));
        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header(":path", "/"));
        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header(":scheme", "http"));
        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header(":authority", "nghttp2.org"));
        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header("accept", "*/*"));
        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header("accept-encoding", "gzip, deflate"));
        ngHttp2ExampleHeaders.add(new HasHeaderFragment.Header("user-agent", "nghttp2/1.15.0"));


        basicResponseHeaders.add(new HasHeaderFragment.Header(":status", "200"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("date", "Tue, 27 Sep 2016 19:41:50 GMT"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("content-type", "text/html"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("set-cookie", "__cfduid=d8bfe297ef26ef6252ea3a822360a6f411475005310; expires=Wed, 27-Sep-17 19:41:50 GMT; path=/; domain=.cloudflare.com; HttpOnly"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("last-modified", "Tue, 27 Sep 2016 17:39:01 GMT"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("cache-control", "public, max-age=14400"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("served-in-seconds", "0.001"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("cf-cache-status", "REVALIDATED"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("expires", "Tue, 27 Sep 2016 23:41:50 GMT"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("server", "cloudflare-nginx"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("cf-ray", "2e916f776c724fd5-DEN"));

        settings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 16384L);
    }

    @Test
    public void testBasicParse() {
        ParserResult result = parser.parse(UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames), dataGen.emptyWrapper(), decoder, settings);
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

    @Test
    public void testParseWithSplitFrame() {
        DataWrapper fullFrames = UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames);
        List<? extends DataWrapper> split = dataGen.split(fullFrames, 6);
        ParserResult result = parser.parse(split.get(0), split.get(1), decoder, settings);
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

//    @Test
//    public void testHigherSplit() {
//        DataWrapper fullFrames = UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames);
//        List<? extends DataWrapper> split = dataGen.split(fullFrames, 12);
//        
//        DataWrapper old = parser.prepareToParse();
//        ParserResult result = parser.parse(old, split.get(0), decoder, settings);
//        Assert.assertFalse(result.hasParsedFrames());
//        Assert.assertFalse(result.hasMoreData());
//        
//        ParserResult nextResult = parser.parse(result.getMoreData(), split.get(1), decoder, settings);
//        Assert.assertTrue(nextResult.hasParsedFrames());
//        Assert.assertFalse(nextResult.hasMoreData());
//        List<Http2Frame> frames = nextResult.getParsedFrames();
//        Assert.assertTrue(frames.size() == 4);        
//    }
    
    @Test
    public void testHigherSplit2() {
        DataWrapper fullFrames = UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames);
        List<? extends DataWrapper> split = dataGen.split(fullFrames, 12);
        
        Http2Memento state = parser2.prepareToParse(decoder);
        parser2.parse(state, split.get(0));
        Assert.assertEquals(0, state.getParsedMessages().size());
        Assert.assertTrue(state.getLeftOverData().getReadableSize() > 0);
        
        parser2.parse(state, split.get(1));
        Assert.assertEquals(4, state.getParsedMessages().size());
        Assert.assertFalse(state.getLeftOverData().getReadableSize() > 0);
    }
    
    @Test
    public void testBasicParseWithPriorData() {
        ParserResult result = parser.parse(
                UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames.subSequence(0, 8).toString()), // oldData
                UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames.substring(8)), // newData
                decoder, settings
            );

        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

    @Test
    public void testBasicParseWithSomeData() {
        ParserResult result = parser.parse(
                UtilsForTest.dataWrapperFromHex(dataFramesWithSomeLeftOverData),
                dataGen.emptyWrapper(),
                decoder, settings);
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertTrue(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
        Assert.assertEquals(UtilsForTest.toHexString(result.getMoreData().createByteArray()), "0000");
    }

    @Test
    public void testBasicParseWithMoreData() {
        ParserResult result = parser.parse(
                UtilsForTest.dataWrapperFromHex(dataFramesWithABunchOfLeftOverData),
                dataGen.emptyWrapper(),
                decoder, settings);
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertTrue(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
        Assert.assertEquals(UtilsForTest.toHexString(result.getMoreData().createByteArray()), "00000800");
    }

    @Test
    public void testBasicParseWithLittleData() {
        ParserResult result = parser.parse(
                UtilsForTest.dataWrapperFromHex("00 00"),
                dataGen.emptyWrapper(),
                decoder, settings);
        Assert.assertFalse(result.hasParsedFrames());
        Assert.assertTrue(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
        Assert.assertEquals(UtilsForTest.toHexString(result.getMoreData().createByteArray()), "0000");
    }

    @Test
    public void testBasicParseWithNoData() {
        ParserResult result = parser.parse(
                dataGen.emptyWrapper(),
                dataGen.emptyWrapper(),
                decoder, settings);
        Assert.assertFalse(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
        Assert.assertEquals(UtilsForTest.toHexString(result.getMoreData().createByteArray()), "");
    }


    @Test
    public void testCreateHugeHeadersFrame() {
        LinkedList<HasHeaderFragment.Header> bigHeaderList = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            bigHeaderList.addAll(basicResponseHeaders);
        }
        Http2SettingsMap remoteSettings = new Http2SettingsMap();
        // set a small max frame size for testing
        remoteSettings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 256L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Http2Frame> headerFrames = parser.createHeaderFrames(bigHeaderList, HEADERS, 0x1, remoteSettings, encoder, out);
        Assert.assertEquals(headerFrames.size(), 2);
        Assert.assertEquals(headerFrames.get(0).getFrameType(), HEADERS);
        Assert.assertEquals(headerFrames.get(1).getFrameType(), Http2FrameType.CONTINUATION);
        Assert.assertEquals(headerFrames.get(0).getStreamId(), 0x1);
        Assert.assertEquals(headerFrames.get(1).getStreamId(), 0x1);
        Assert.assertTrue(((HasHeaderFragment) headerFrames.get(1)).isEndHeaders());
        Assert.assertFalse(((HasHeaderFragment) headerFrames.get(0)).isEndHeaders());
    }

    @Test
    public void testCreateHugePushPromiseFrame() {
        LinkedList<HasHeaderFragment.Header> bigHeaderList = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            bigHeaderList.addAll(basicResponseHeaders);
        }
        Http2SettingsMap remoteSettings = new Http2SettingsMap();
        // set a small max frame size for testing
        remoteSettings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 256L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Http2Frame> headerFrames = parser.createHeaderFrames(bigHeaderList, Http2FrameType.PUSH_PROMISE, 0x1, remoteSettings, encoder, out);
        Assert.assertEquals(headerFrames.size(), 2);
        Assert.assertEquals(headerFrames.get(0).getFrameType(), Http2FrameType.PUSH_PROMISE);
        Assert.assertEquals(headerFrames.get(1).getFrameType(), Http2FrameType.CONTINUATION);
        Assert.assertTrue(((HasHeaderFragment) headerFrames.get(1)).isEndHeaders());
        Assert.assertFalse(((HasHeaderFragment) headerFrames.get(0)).isEndHeaders());
        Assert.assertEquals(((Http2PushPromise) headerFrames.get(0)).getPromisedStreamId(), 0x1);
        Assert.assertEquals(headerFrames.get(1).getStreamId(), 0x1);
    }

    @Test
    public void testParseWithHeaderFrames() {
        LinkedList<HasHeaderFragment.Header> bigHeaderList = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            bigHeaderList.addAll(basicResponseHeaders);
        }
        Http2SettingsMap remoteSettings = new Http2SettingsMap();
        // set a small max frame size for testing
        remoteSettings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 256L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        List<Http2Frame> headerFrames = parser.createHeaderFrames(bigHeaderList, Http2FrameType.HEADERS, 0x1, remoteSettings, encoder, out);

        Assert.assertEquals(headerFrames.size(), 2);
        DataWrapper serializedHeaderFrames = parser.marshal(headerFrames);
        DataWrapper combined = dataGen.chainDataWrappers(
                UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames),
                dataGen.chainDataWrappers(
                        serializedHeaderFrames,
                        UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames)));

        ParserResult result = parser.parse(combined, dataGen.emptyWrapper(), decoder, settings);
        Assert.assertEquals(result.getParsedFrames().size(), 9); // there should be 8 data frames and one header frame
        Http2Frame headerFrame = result.getParsedFrames().get(4);
        Assert.assertEquals(headerFrame.getFrameType(), HEADERS);
        Assert.assertEquals(((Http2Headers) headerFrame).getHeaderList().size(), basicResponseHeaders.size() * 5);
    }

    @Test
    public void testSerializeHeaders() {

        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        DataWrapper serializedExample = parser.serializeHeaders(ngHttp2ExampleHeaders, encoder, out2);
        String serializedExampleHex = UtilsForTest.toHexString(serializedExample.createByteArray());
        Assert.assertArrayEquals(
                UtilsForTest.toByteArray(serializedExampleHex),
                UtilsForTest.toByteArray(ngHttp2ExampleHeaderFragment));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataWrapper serialized = parser.serializeHeaders(basicRequestHeaders, encoder, out);
        String serializedHex = UtilsForTest.toHexString(serialized.createByteArray());
        Assert.assertArrayEquals(
                UtilsForTest.toByteArray(serializedHex),
                UtilsForTest.toByteArray(basicRequestSerializationNghttp2)
        );

    }

    @Test
    public void testDeserializeHeaders() {
        List<HasHeaderFragment.Header> headers = parser.deserializeHeaders(UtilsForTest.dataWrapperFromHex(ngHttp2ExampleHeaderFragment), decoder);
        Assert.assertEquals(headers, ngHttp2ExampleHeaders);

        List<HasHeaderFragment.Header> headers2 = parser.deserializeHeaders(UtilsForTest.dataWrapperFromHex(basicRequestSerializationNghttp2), decoder);
        Assert.assertEquals(headers2, basicRequestHeaders);


    }

}
