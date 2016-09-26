package com.webpieces.http2parser.dto;

import dto.Http2Data;
import dto.Http2Frame;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Http2Frame frame = Util.frameFromHex(unpaddedDataFrame);
        Assert.assertTrue(Http2Data.class.isInstance(frame));

        Http2Data castFrame = (Http2Data) frame;

        byte[] data = castFrame.getData().createByteArray();
        Assert.assertArrayEquals(data, Util.toByteArray("FF FF FF FF FF FF FF FF"));
        Assert.assertFalse(castFrame.isEndStream());
    }

    @Test
    public void testParsePaddedData() {
        Http2Frame frame = Util.frameFromHex(paddedDataFrame);

        Assert.assertTrue(Http2Data.class.isInstance(frame));
        Http2Data castFrame = (Http2Data) frame;

        // Even though there is padding the data should be the same
        byte[] data = castFrame.getData().createByteArray();
        Assert.assertArrayEquals(data, Util.toByteArray("FF FF FF FF FF FF FF FF"));
        Assert.assertFalse(castFrame.isEndStream());
    }

    @Test
    public void testCreateDataFrameUnpadded() {
        Http2Data frame = new Http2Data();
        frame.setData(Util.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        Assert.assertArrayEquals(frame.getBytes(), Util.toByteArray(unpaddedDataFrame));
        Assert.assertTrue(Util.isReservedBitZero(frame.getDataWrapper()));
    }

    @Test public void testCreateDataFramePadded() {
        Http2Data frame = new Http2Data();
        frame.setData(Util.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setPadding(Util.toByteArray("00 00"));
        Assert.assertArrayEquals(frame.getBytes(), Util.toByteArray(paddedDataFrame));
        Assert.assertTrue(Util.isReservedBitZero(frame.getDataWrapper()));
    }

    @Test
    public void testCreateDataFrameEndStream() {
        Http2Data frame = new Http2Data();
        frame.setData(Util.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndStream();
        Assert.assertArrayEquals(frame.getBytes(), Util.toByteArray(endStreamDataFrame));
        Assert.assertTrue(Util.isReservedBitZero(frame.getDataWrapper()));
    }

    @Test
    public void testCreateDataFramePaddedEndStream() {
        Http2Data frame = new Http2Data();
        frame.setData(Util.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndStream();
        frame.setPadding(Util.toByteArray("00 00"));
        Assert.assertArrayEquals(frame.getBytes(), Util.toByteArray(paddedEndStreamDataFrame));
        Assert.assertTrue(Util.isReservedBitZero(frame.getDataWrapper()));
    }
}
