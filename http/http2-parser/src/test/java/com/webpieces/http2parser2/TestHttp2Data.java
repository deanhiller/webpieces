package com.webpieces.http2parser2;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;

import com.webpieces.http2parser.UtilsForTest;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.DataFrame;

public class TestHttp2Data{
	
    private static Http2Parser2 parser = Http2ParserFactory.createParser2(new BufferCreationPool());

    private static String unpaddedDataFrame() {
    	String data = 
            "00 00 08" + // Length
            "00" + // Type
            "00" + // Flags
            "00 00 00 01" + // R + streamid
            "FF FF FF FF FF FF FF FF"; // payload
    	return data.replaceAll("\\s+","");
    }

    private static String endStreamDataFrame() {
    	String data =
            "00 00 08" + // Length
            "00" + // Type
            "01" + // Flags - endStream = true
            "00 00 00 01" + // R + streamid
            "FF FF FF FF FF FF FF FF"; // payload
        return data.replaceAll("\\s+","");
    }

    private static String paddedDataFrame() {
    	String data =
            "00 00 0B" + // Length
            "00" + // Type
            "08" + // Flags (padded = true)
            "00 00 00 01" + // R + streamid
            "02" + // padding length
            "FF FF FF FF FF FF FF FF" + // data
            "00 00"; // padding
        return data.replaceAll("\\s+","");
    }

    private static String paddedEndStreamDataFrame() {
    	String data =
            "00 00 0B" + // Length
            "00" + // Type
            "09" + // Flags - endStream = true
            "00 00 00 01" + // R + streamid
            "02" + // padding length
            "FF FF FF FF FF FF FF FF" + // payload
            "00 00"; // padding
        return data.replaceAll("\\s+","");
    }

//    @Test
//    public void testParseUnpaddedData() {
//        Http2Frame frame = UtilsForTest.frameFromHex(unpaddedDataFrame);
//        Assert.assertTrue(Http2Data.class.isInstance(frame));
//
//        Http2Data castFrame = (Http2Data) frame;
//
//        byte[] data = castFrame.getData().createByteArray();
//        Assert.assertArrayEquals(data, UtilsForTest.toByteArray("FF FF FF FF FF FF FF FF"));
//        Assert.assertFalse(castFrame.isEndStream());
//
//        UtilsForTest.testBidiFromBytes(unpaddedDataFrame);
//    }
//
//    @Test
//    public void testParsePaddedData() {
//        Http2Frame frame = UtilsForTest.frameFromHex(paddedDataFrame);
//
//        Assert.assertTrue(Http2Data.class.isInstance(frame));
//        Http2Data castFrame = (Http2Data) frame;
//
//        // Even though there is padding the data should be the same
//        byte[] data = castFrame.getData().createByteArray();
//        Assert.assertArrayEquals(data, UtilsForTest.toByteArray("FF FF FF FF FF FF FF FF"));
//        Assert.assertFalse(castFrame.isEndStream());
//        UtilsForTest.testBidiFromBytes(paddedDataFrame);
//    }

    @Test
    public void testCreateDataFrameUnpadded() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(unpaddedDataFrame(), hexFrame);
    }

    @Test public void testCreateDataFramePadded() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setPadding(UtilsForTest.toByteArray("00 00"));

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(paddedDataFrame(), hexFrame);
    }

    @Test
    public void testCreateDataFrameEndStream() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndStream(true);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(endStreamDataFrame(), hexFrame);
    }

    @Test
    public void testCreateDataFramePaddedEndStream() {
        DataFrame frame = new DataFrame();
        frame.setData(UtilsForTest.dataWrapperFromHex("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndStream(true);
        frame.setPadding(UtilsForTest.toByteArray("00 00"));

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(paddedEndStreamDataFrame(), hexFrame);
    }
}
