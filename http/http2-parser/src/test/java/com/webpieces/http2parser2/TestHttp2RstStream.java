package com.webpieces.http2parser2;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;

import com.webpieces.http2parser.UtilsForTest;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2RstStream;

public class TestHttp2RstStream {
	
    private static Http2Parser2 parser = Http2ParserFactory.createParser2(new BufferCreationPool());

    private static String connectError() {
    	String data =
            "00 00 04" + // length
            "03" + // type
            "00" + // flags
            "00 00 00 04" + // streamid
            "00 00 00 0A"; // payload
    	return data.replaceAll("\\s+","");
    }
    @Test
    public void testCreateRstStream() {
        Http2RstStream frame = new Http2RstStream();
        frame.setStreamId(0x4);
        frame.setErrorCode(Http2ErrorCode.CONNECT_ERROR);

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
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
