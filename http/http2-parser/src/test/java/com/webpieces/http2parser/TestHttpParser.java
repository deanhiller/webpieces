package com.webpieces.http2parser;

import com.webpieces.http2parser.api.HttpParser;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.dto.Http2Frame;
import com.webpieces.http2parser.impl.HttpParserImpl;
import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.util.List;

public class TestHttpParser {
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

    private static HttpParser parser = new HttpParserImpl();
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    @Test
    public void testBasicParse() {
        ParserResult result = parser.parse(Util.dataWrapperFromHex(aBunchOfDataFrames), dataGen.emptyWrapper());
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }


    @Test
    public void testBasicParseWithPriorData() {
        ParserResult result = parser.parse(
                Util.dataWrapperFromHex(aBunchOfDataFrames.subSequence(0, 8).toString()), // oldData
                Util.dataWrapperFromHex(aBunchOfDataFrames.substring(8)) // newData
            );

        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

    @Test
    public void testBasicParseWithSomeData() {
        ParserResult result = parser.parse(
                Util.dataWrapperFromHex(dataFramesWithSomeLeftOverData),
                dataGen.emptyWrapper());
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertTrue(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
        Assert.assertEquals(Util.toHexString(result.getMoreData().createByteArray()), "0000");
    }

    @Test
    public void testBasicParseWithMoreData() {
        ParserResult result = parser.parse(
                Util.dataWrapperFromHex(dataFramesWithABunchOfLeftOverData),
                dataGen.emptyWrapper());
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertTrue(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
        Assert.assertEquals(Util.toHexString(result.getMoreData().createByteArray()), "00000800");
    }

    @Test
    public void testBasicParseWithLittleData() {
        ParserResult result = parser.parse(
                Util.dataWrapperFromHex("00 00"),
                dataGen.emptyWrapper());
        Assert.assertFalse(result.hasParsedFrames());
        Assert.assertTrue(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
        Assert.assertEquals(Util.toHexString(result.getMoreData().createByteArray()), "0000");
    }

    @Test
    public void testBasicParseWithNoData() {
        ParserResult result = parser.parse(
                dataGen.emptyWrapper(),
                dataGen.emptyWrapper());
        Assert.assertFalse(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
        Assert.assertEquals(Util.toHexString(result.getMoreData().createByteArray()), "");
    }
}
