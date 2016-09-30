package com.webpieces.http2parser;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.ParserResult;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.helpers.Util;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import javax.xml.crypto.Data;
import java.util.List;

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

    @Test
    public void testBasicParse() {
        ParserResult result = parser.parse(UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames), dataGen.emptyWrapper());
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }

    @Test
    public void testParseWithSplitFrame() {
        DataWrapper fullFrames = UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames);
        List<? extends DataWrapper> split = dataGen.split(fullFrames, 6);
        ParserResult result = parser.parse(split.get(0), split.get(1));
        Assert.assertTrue(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 4);
    }


    @Test
    public void testBasicParseWithPriorData() {
        ParserResult result = parser.parse(
                UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames.subSequence(0, 8).toString()), // oldData
                UtilsForTest.dataWrapperFromHex(aBunchOfDataFrames.substring(8)) // newData
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
                dataGen.emptyWrapper());
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
                dataGen.emptyWrapper());
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
                dataGen.emptyWrapper());
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
                dataGen.emptyWrapper());
        Assert.assertFalse(result.hasParsedFrames());
        Assert.assertFalse(result.hasMoreData());
        List<Http2Frame> frames = result.getParsedFrames();
        Assert.assertTrue(frames.size() == 0);
        Assert.assertEquals(UtilsForTest.toHexString(result.getMoreData().createByteArray()), "");
    }
}
