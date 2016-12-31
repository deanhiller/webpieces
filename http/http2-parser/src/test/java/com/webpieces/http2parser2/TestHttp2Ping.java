package com.webpieces.http2parser2;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;

import com.webpieces.http2parser.UtilsForTest;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.PingFrame;

public class TestHttp2Ping {
	
    private static Http2Parser2 parser = Http2ParserFactory.createParser2(new BufferCreationPool());

    private static String getPingFrame() {
            String ping = 
            "00 00 08" + // length
            "06" + // frame type
            "00" + // flags
            "00 00 00 00" + //R + streamid
            "00 00 00 00 00 00 00 10"; // opaqueData
            return ping.replaceAll("\\s+","");
    }

    private static String getPongFrame() {
    	String pong = 
            "00 00 08" + // length
            "06" + // frame type
            "01" + // flags
            "00 00 00 00" + //R + streamid
            "00 00 00 00 00 00 00 10"; // opaqueData
    	
        return pong.replaceAll("\\s+","");
    }

    @Test
    public void testCreatePingFrame() {
        PingFrame frame = new PingFrame();
        frame.setOpaqueData(0x10);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(getPingFrame(), hexFrame);
    }

    
    @Test
    public void testCreatePongFrame() {
        PingFrame frame = new PingFrame();
        frame.setOpaqueData(0x10);
        frame.setIsPingResponse(true);

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(getPongFrame(), hexFrame);
    }
//
//    @Test
//    public void testParsePingFrame() {
//        Http2Frame frame = UtilsForTest.frameFromHex(pingFrame);
//        Assert.assertTrue(Http2Ping.class.isInstance(frame));
//
//        Http2Ping castFrame = (Http2Ping) frame;
//        Assert.assertFalse(castFrame.isPingResponse());
//        Assert.assertEquals(castFrame.getOpaqueData(), 0x10);
//    }
//
//    @Test
//    public void testParsePongFrame() {
//        Http2Frame frame = UtilsForTest.frameFromHex(pongFrame);
//        Assert.assertTrue(Http2Ping.class.isInstance(frame));
//
//        Http2Ping castFrame = (Http2Ping) frame;
//        Assert.assertTrue(castFrame.isPingResponse());
//        Assert.assertEquals(castFrame.getOpaqueData(), 0x10);
//    }
}
