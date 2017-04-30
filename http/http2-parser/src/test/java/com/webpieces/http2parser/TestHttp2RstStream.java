package com.webpieces.http2parser;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class TestHttp2RstStream {
	
    private static Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());

    private static String connectError() {
    	String data =
            "00 00 04" + // length
            "03" + // type
            "00" + // flags
            "00 00 00 04" + // streamid
            "00 00 00 0A"; // payload
    	return data.replaceAll("\\s+","");
    }
    
	private Http2Memento memento;

    @Before
    public void setUp() {
    	memento = parser.prepareToParse(Long.MAX_VALUE);    	
    }
    
    @Test
    public void testParseRstStream() {
    	DataWrapper data = Util.hexToBytes(connectError());
    	parser.parse(memento, data);
    	
    	RstStreamFrame frame = (RstStreamFrame) assertGood();
    	Assert.assertEquals(4, frame.getStreamId());
    	Assert.assertEquals(10, frame.getErrorCode());
    }

	private Http2Frame assertGood() {
		Assert.assertEquals(0, memento.getLeftOverData().getReadableSize());
    	List<Http2Frame> frames = memento.getParsedFrames();
    	Assert.assertEquals(1, frames.size());
    	return frames.get(0);
	}
	
    @Test
    public void testMarshalRstStream() {
        RstStreamFrame frame = new RstStreamFrame();
        frame.setStreamId(0x4);
        frame.setKnownErrorCode(Http2ErrorCode.CONNECT_ERROR);

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(connectError(), hexFrame);
    }

//    @Test public void testParseRstStream() {
//        Http2Frame frame = UtilsForTest.frameFromHex(connectError);
//        Assert.assertTrue(Http2RstStream.class.isInstance(frame));
//        Http2RstStream castFrame = (Http2RstStream) frame;
//
//        Assert.assertEquals(castFrame.getStreamId(), 4);
//        Assert.assertEquals(castFrame.getErrorCode(), Http2ErrorCode.CONNECT_ERROR);
//    }
}
