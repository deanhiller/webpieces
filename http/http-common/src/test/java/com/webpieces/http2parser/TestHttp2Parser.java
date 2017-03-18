package com.webpieces.http2parser;

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

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
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

}
