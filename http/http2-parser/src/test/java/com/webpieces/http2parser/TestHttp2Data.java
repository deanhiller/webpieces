package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;

public class TestHttp2Data{
    private static String unpaddedDataFrame =
            "00 00 08" + // Length
            "00" + // Type
            "00" + // Flags
            "00 00 00 01" + // R + streamid
            "FF FF FF FF FF FF FF FF"; // payload

    private static String endStreamDataFrame =
            "00 00 08" + // Length
            "00" + // Type
            "01" + // Flags - endStream = true
            "00 00 00 01" + // R + streamid
            "FF FF FF FF FF FF FF FF"; // payload

    private static String paddedDataFrame =
            "00 00 0B" + // Length
            "00" + // Type
            "08" + // Flags (padded = true)
            "00 00 00 01" + // R + streamid
            "02" + // padding length
            "FF FF FF FF FF FF FF FF" + // data
            "00 00"; // padding

    private static String paddedEndStreamDataFrame =
            "00 00 0B" + // Length
            "00" + // Type
            "09" + // Flags - endStream = true
            "00 00 00 01" + // R + streamid
            "02" + // padding length
            "FF FF FF FF FF FF FF FF" + // payload
            "00 00"; // padding

    @Test
    public void testParseUnpaddedData() {
        AbstractHttp2Frame frame = UtilsForTest.frameFromHex(unpaddedDataFrame);
        Assert.assertTrue(DataFrame.class.isInstance(frame));

        DataFrame castFrame = (DataFrame) frame;

        byte[] data = castFrame.getData().createByteArray();
        Assert.assertArrayEquals(data, UtilsForTest.toByteArray("FF FF FF FF FF FF FF FF"));
        Assert.assertFalse(castFrame.isEndStream());

        UtilsForTest.testBidiFromBytes(unpaddedDataFrame);
    }

    @Test
    public void testParsePaddedData() {
        AbstractHttp2Frame frame = UtilsForTest.frameFromHex(paddedDataFrame);

        Assert.assertTrue(DataFrame.class.isInstance(frame));
        DataFrame castFrame = (DataFrame) frame;

        // Even though there is padding the data should be the same
        byte[] data = castFrame.getData().createByteArray();
        Assert.assertArrayEquals(data, UtilsForTest.toByteArray("FF FF FF FF FF FF FF FF"));
        Assert.assertFalse(castFrame.isEndStream());
        UtilsForTest.testBidiFromBytes(paddedDataFrame);
    }

    @Test
    public void testCreateDataFrameUnpadded() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        Assert.assertArrayEquals(UtilsForTest.frameToBytes(frame), UtilsForTest.toByteArray(unpaddedDataFrame));
        Assert.assertTrue(UtilsForTest.isReservedBitZero(UtilsForTest.frameToDataWrapper(frame)));
        UtilsForTest.testBidiFromBytes(UtilsForTest.frameToHex(frame));
    }

    @Test public void testCreateDataFramePadded() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setPadding(UtilsForTest.toByteArray("00 00"));
        Assert.assertArrayEquals(UtilsForTest.frameToBytes(frame), UtilsForTest.toByteArray(paddedDataFrame));
        Assert.assertTrue(UtilsForTest.isReservedBitZero(UtilsForTest.frameToDataWrapper(frame)));
        UtilsForTest.testBidiFromBytes(UtilsForTest.frameToHex(frame));
    }

    @Test
    public void testCreateDataFrameEndStream() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndStream(true);
        Assert.assertArrayEquals(UtilsForTest.frameToBytes(frame), UtilsForTest.toByteArray(endStreamDataFrame));
        Assert.assertTrue(UtilsForTest.isReservedBitZero(UtilsForTest.frameToDataWrapper(frame)));
        UtilsForTest.testBidiFromBytes(UtilsForTest.frameToHex(frame));
    }

    @Test
    public void testCreateDataFramePaddedEndStream() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndStream(true);
        frame.setPadding(UtilsForTest.toByteArray("00 00"));
        Assert.assertArrayEquals(UtilsForTest.frameToBytes(frame), UtilsForTest.toByteArray(paddedEndStreamDataFrame));
        Assert.assertTrue(UtilsForTest.isReservedBitZero(UtilsForTest.frameToDataWrapper(frame)));
        UtilsForTest.testBidiFromBytes(UtilsForTest.frameToHex(frame));
    }
}
