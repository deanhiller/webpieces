package com.webpieces.http2parser;

import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.HEADERS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.temp.HeaderDecoding2;
import org.webpieces.httpcommon.temp.HeaderEncoding2;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

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

    private static HpackParser parser = HpackParserFactory.createParser(new BufferCreationPool(), true);
    private static Http2Parser parser2 = Http2ParserFactory.createParser(new BufferCreationPool());

    private static LinkedList<Http2Header> basicRequestHeaders = new LinkedList<>();
    private static LinkedList<Http2Header> basicResponseHeaders = new LinkedList<>();
    private static LinkedList<Http2Header> ngHttp2ExampleHeaders = new LinkedList<>();

    private Encoder encoder = new Encoder(4096);
    private HeaderEncoding2 encoding = new HeaderEncoding2(encoder, Integer.MAX_VALUE);
	private int maxFrameSize;
	private int maxHeaderSize = 4096;
	private int maxHeaderTableSize = 4096;

    private static List<Http2Setting> settings = new ArrayList<>();

    // https://github.com/http2jp/hpack-test-case/blob/master/nghttp2/story_00.json
    private static String basicRequestSerializationNghttp2 = "82864188f439ce75c875fa5784";

    private static String ngHttp2ExampleHeaderFragment =
            "82 84 86 41 88 aa 69 d2 9a c4 b9 ec 9b 53 03 2a 2f" +
            "2a 90 7a 8a aa 69 d2 9a c4 c0 57 0b 6b 83";
    static {
        basicRequestHeaders.add(new Http2Header(":method", "GET"));
        basicRequestHeaders.add(new Http2Header(":scheme", "http"));
        basicRequestHeaders.add(new Http2Header(":authority", "yahoo.co.jp"));
        basicRequestHeaders.add(new Http2Header(":path", "/"));

        ngHttp2ExampleHeaders.add(new Http2Header(":method", "GET"));
        ngHttp2ExampleHeaders.add(new Http2Header(":path", "/"));
        ngHttp2ExampleHeaders.add(new Http2Header(":scheme", "http"));
        ngHttp2ExampleHeaders.add(new Http2Header(":authority", "nghttp2.org"));
        ngHttp2ExampleHeaders.add(new Http2Header("accept", "*/*"));
        ngHttp2ExampleHeaders.add(new Http2Header("accept-encoding", "gzip, deflate"));
        ngHttp2ExampleHeaders.add(new Http2Header("user-agent", "nghttp2/1.15.0"));


        basicResponseHeaders.add(new Http2Header(":status", "200"));
        basicResponseHeaders.add(new Http2Header("date", "Tue, 27 Sep 2016 19:41:50 GMT"));
        basicResponseHeaders.add(new Http2Header("content-type", "text/html"));
        basicResponseHeaders.add(new Http2Header("set-cookie", "__cfduid=d8bfe297ef26ef6252ea3a822360a6f411475005310; expires=Wed, 27-Sep-17 19:41:50 GMT; path=/; domain=.cloudflare.com; HttpOnly"));
        basicResponseHeaders.add(new Http2Header("last-modified", "Tue, 27 Sep 2016 17:39:01 GMT"));
        basicResponseHeaders.add(new Http2Header("cache-control", "public, max-age=14400"));
        basicResponseHeaders.add(new Http2Header("served-in-seconds", "0.001"));
        basicResponseHeaders.add(new Http2Header("cf-cache-status", "REVALIDATED"));
        basicResponseHeaders.add(new Http2Header("expires", "Tue, 27 Sep 2016 23:41:50 GMT"));
        basicResponseHeaders.add(new Http2Header("server", "cloudflare-nginx"));
        basicResponseHeaders.add(new Http2Header("cf-ray", "2e916f776c724fd5-DEN"));

        settings.add(new Http2Setting(SettingsParameter.SETTINGS_MAX_FRAME_SIZE, 16384L));
    }

    @Before
    public void setUp() {
    	maxFrameSize = Integer.MAX_VALUE;
    }
    
    @Test
    public void testBasicParse() {
    	DataWrapper data = UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames);
    	UnmarshalState state = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
    	UnmarshalState result = parser.unmarshal(state, data);
        Assert.assertEquals(0, result.getLeftOverDataSize());
        List<Http2Msg> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

    @Test
    public void testParseWithSplitFrame() {
        DataWrapper fullFrames = UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames);
        List<? extends DataWrapper> split = dataGen.split(fullFrames, 6);
        
    	UnmarshalState state = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
        state = parser.unmarshal(state, split.get(0));
        state = parser.unmarshal(state, split.get(1));

        Assert.assertEquals(0, state.getLeftOverDataSize());
        List<Http2Msg> frames = state.getParsedFrames();
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
        DataWrapper fullFrames = UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames);
        List<? extends DataWrapper> split = dataGen.split(fullFrames, 12);
        
        Http2Memento state = parser2.prepareToParse(Integer.MAX_VALUE);
        parser2.parse(state, split.get(0));
        Assert.assertEquals(0, state.getParsedFrames().size());
        Assert.assertTrue(state.getLeftOverData().getReadableSize() > 0);
        
        parser2.parse(state, split.get(1));
        Assert.assertEquals(4, state.getParsedFrames().size());
        Assert.assertFalse(state.getLeftOverData().getReadableSize() > 0);
    }
    
    @Test
    public void testBasicParseWithPriorData() {
    	UnmarshalState result = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);

        result = parser.unmarshal(result, 
                UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames.subSequence(0, 8).toString()) // oldData
            );
        result = parser.unmarshal(result, 
                UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames.substring(8)) // newData
            );

        Assert.assertEquals(0, result.getLeftOverDataSize());
        List<Http2Msg> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

    @Test
    public void testBasicParseWithSomeData() {
    	UnmarshalState result = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
        result = parser.unmarshal(result, 
                UtilsForTest2.dataWrapperFromHex(dataFramesWithSomeLeftOverData));
        Assert.assertTrue(result.getLeftOverDataSize() > 0);

        List<Http2Msg> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
        //Assert.assertEquals(UtilsForTest2.toHexString(result.getLeftOverData().createByteArray()), "0000");
    }

    @Test
    public void testBasicParseWithMoreData() {
    	UnmarshalState result = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
        result = parser.unmarshal(result, 
                UtilsForTest2.dataWrapperFromHex(dataFramesWithABunchOfLeftOverData));
        Assert.assertTrue(result.getLeftOverDataSize() > 0);
        List<Http2Msg> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
        //Assert.assertEquals(UtilsForTest2.toHexString(result.getLeftOverData().createByteArray()), "00000800");
    }

    @Test
    public void testBasicParseWithLittleData() {
    	UnmarshalState result = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
        result = parser.unmarshal(result, 
                UtilsForTest2.dataWrapperFromHex("00 00"));
        Assert.assertTrue(result.getLeftOverDataSize() > 0);
        List<Http2Msg> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
        //Assert.assertEquals(UtilsForTest2.toHexString(result.getLeftOverData().createByteArray()), "0000");
    }

    @Test
    public void testBasicParseWithNoData() {
    	UnmarshalState result = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
        result = parser.unmarshal(result,
                dataGen.emptyWrapper());
        Assert.assertEquals(0, result.getLeftOverDataSize());
        List<Http2Msg> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
    }


    @Test
    public void testCreateHugeHeadersFrame() {
        LinkedList<Http2Header> bigHeaderList = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            bigHeaderList.addAll(basicResponseHeaders);
        }
        // set a small max frame size for testing
        encoding.setMaxFrameSize(256);

        HeadersFrame initialFrame = new HeadersFrame();
        initialFrame.setStreamId(1);
        List<Http2Frame> headerFrames = encoding.createHeaderFrames(initialFrame, bigHeaderList);
        
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
        LinkedList<Http2Header> bigHeaderList = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            bigHeaderList.addAll(basicResponseHeaders);
        }
        // set a small max frame size for testing
        encoding.setMaxFrameSize(256);

        List<Http2Frame> headerFrames = encoding.createPushPromises(bigHeaderList, 1, 2);
        
        Assert.assertEquals(headerFrames.size(), 2);
        Assert.assertEquals(headerFrames.get(0).getFrameType(), Http2FrameType.PUSH_PROMISE);
        Assert.assertEquals(headerFrames.get(1).getFrameType(), Http2FrameType.CONTINUATION);
        Assert.assertTrue(((HasHeaderFragment) headerFrames.get(1)).isEndHeaders());
        Assert.assertFalse(((HasHeaderFragment) headerFrames.get(0)).isEndHeaders());
        Assert.assertEquals(((PushPromiseFrame) headerFrames.get(0)).getPromisedStreamId(), 2);
        Assert.assertEquals(headerFrames.get(1).getStreamId(), 0x1);
    }

    @Test
    public void testParseWithHeaderFrames() {
        LinkedList<Http2Header> bigHeaderList = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            bigHeaderList.addAll(basicResponseHeaders);
        }
        // set a small max frame size for testing
        encoding.setMaxFrameSize(256);

        HeadersFrame initialFrame = new HeadersFrame();
        initialFrame.setStreamId(1);
        List<Http2Frame> headerFrames = encoding.createHeaderFrames(initialFrame, bigHeaderList);

        Assert.assertEquals(headerFrames.size(), 2);
        DataWrapper serializedHeaderFrames = translate(headerFrames);
        DataWrapper combined = dataGen.chainDataWrappers(
                UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames),
                dataGen.chainDataWrappers(
                        serializedHeaderFrames,
                        UtilsForTest2.dataWrapperFromHex(aBunchOfDataFrames)));

    	UnmarshalState result = parser.prepareToUnmarshal(maxHeaderSize, maxHeaderTableSize, maxFrameSize);
        result = parser.unmarshal(result, combined);
        Assert.assertEquals(result.getParsedFrames().size(), 9); // there should be 8 data frames and one header frame
        Http2Msg headerFrame = result.getParsedFrames().get(4);
        Assert.assertEquals(headerFrame.getClass(), Http2Headers.class);
        Assert.assertEquals(((Http2Headers) headerFrame).getHeaders().size(), basicResponseHeaders.size() * 5);
    }

    private DataWrapper translate(List<Http2Frame> frames) {
    	DataWrapper allData = dataGen.emptyWrapper();
    	for(Http2Frame f : frames) {
    		DataWrapper data = parser2.marshal(f);
    		allData = dataGen.chainDataWrappers(allData, data);
    	}
		return allData;
	}
    
    @Test
    public void testSerializeHeaders() {

        DataWrapper serializedExample = encoding.serializeHeaders(ngHttp2ExampleHeaders);
        String serializedExampleHex = UtilsForTest2.toHexString(serializedExample.createByteArray());
        Assert.assertArrayEquals(
                UtilsForTest2.toByteArray(serializedExampleHex),
                UtilsForTest2.toByteArray(ngHttp2ExampleHeaderFragment));

        DataWrapper serialized = encoding.serializeHeaders(basicRequestHeaders);
        String serializedHex = UtilsForTest2.toHexString(serialized.createByteArray());
        Assert.assertArrayEquals(
                UtilsForTest2.toByteArray(serializedHex),
                UtilsForTest2.toByteArray(basicRequestSerializationNghttp2)
        );

    }

    @Test
    public void testDeserializeHeaders() {
    	Decoder decoder = new Decoder(4096, 4096);
    	HeaderDecoding2 decoding = new HeaderDecoding2(decoder);
    	
        List<Http2Header> headers = decoding.decode(UtilsForTest2.dataWrapperFromHex(ngHttp2ExampleHeaderFragment));
        Assert.assertEquals(headers, ngHttp2ExampleHeaders);

        List<Http2Header> headers2 = decoding.decode(UtilsForTest2.dataWrapperFromHex(basicRequestSerializationNghttp2));
        Assert.assertEquals(headers2, basicRequestHeaders);


    }

}
