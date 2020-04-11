package com.webpieces.http2parser;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.TwoPools;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestHttp2Data{
	
    private static Http2Parser parser = Http2ParserFactory.createParser(new TwoPools("pl", new SimpleMeterRegistry()));

    private static String unpaddedDataFrame() {
    	String data = 
            "00 00 08" + // Length
            "00" + // Type
            "01" + // Flags
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
            "09" + // Flags (padded = true)
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

	private Http2Memento memento;

    @Before
    public void setUp() {
    	memento = parser.prepareToParse(Long.MAX_VALUE);    	
    }
    
    @Test
    public void testParseUnpaddedData() {
    	DataWrapper data = Util.hexToBytes(unpaddedDataFrame());
    	parser.parse(memento, data);
    	
    	DataFrame frame = (DataFrame) assertGood();
    	Assert.assertEquals(1, frame.getStreamId());
    	Assert.assertEquals(8, frame.getData().getReadableSize());
    	Assert.assertEquals(0, frame.getPadding().getReadableSize());  
    	Assert.assertTrue(frame.isEndOfStream());
    }

    @Test
    public void testParsePaddedData() {
    	DataWrapper data = Util.hexToBytes(paddedDataFrame());
    	parser.parse(memento, data);
    	
    	DataFrame frame = (DataFrame) assertGood();
    	Assert.assertEquals(1, frame.getStreamId());
    	Assert.assertEquals(8, frame.getData().getReadableSize());
    	Assert.assertEquals(2, frame.getPadding().getReadableSize());  
    	Assert.assertTrue(frame.isEndOfStream());
    }

    @Test
    public void testParseEndOfStreamTrue() {
    	DataWrapper data = Util.hexToBytes(endStreamDataFrame());
    	parser.parse(memento, data);
    	
    	DataFrame frame = (DataFrame) assertGood();
    	Assert.assertEquals(1, frame.getStreamId());
    	Assert.assertEquals(8, frame.getData().getReadableSize());
    	Assert.assertEquals(0, frame.getPadding().getReadableSize());  
    	Assert.assertTrue(frame.isEndOfStream());
    }
    
	private Http2Frame assertGood() {
		Assert.assertEquals(0, memento.getLeftOverDataSize());
    	List<Http2Frame> frames = memento.getParsedFrames();
    	Assert.assertEquals(1, frames.size());
    	return frames.get(0);
	}

    @Test
    public void testMarshalDataFrameUnpadded() {
        DataFrame frame = new DataFrame();
        frame.setData(Util.hexToBytes("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(unpaddedDataFrame(), hexFrame);
    }

    @Test 
    public void testMarshalDataFramePadded() {
        DataFrame frame = new DataFrame();
        frame.setData(Util.hexToBytes("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setPadding(Util.hexToBytes("00 00"));

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(paddedDataFrame(), hexFrame);
    }

    @Test
    public void testMarshalDataFrameEndStream() {
        DataFrame frame = new DataFrame();
        frame.setData(Util.hexToBytes("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndOfStream(true);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(endStreamDataFrame(), hexFrame);
    }

    @Test
    public void testMarshalDataFramePaddedEndStream() {
        DataFrame frame = new DataFrame();
        frame.setData(Util.hexToBytes("FF FF FF FF FF FF FF FF"));
        frame.setStreamId(1);
        frame.setEndOfStream(true);
        frame.setPadding(Util.hexToBytes("00 00"));

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(paddedEndStreamDataFrame(), hexFrame);
    }
}
