package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2Ping;

public class TestHttp2Ping {
    private static String pingFrame =
            "00 00 08" + // length
            "06" + // frame type
            "00" + // flags
            "00 00 00 00" + //R + streamid
            "00 00 00 00 00 00 00 10"; // opaqueData

    private static String pongFrame =
            "00 00 08" + // length
            "06" + // frame type
            "01" + // flags
            "00 00 00 00" + //R + streamid
            "00 00 00 00 00 00 00 10"; // opaqueData


    @Test
    public void testCreatePingFrame() {
        Http2Ping frame = new Http2Ping();
        frame.setOpaqueData(0x10);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testCreatePongFrame() {
        Http2Ping frame = new Http2Ping();
        frame.setOpaqueData(0x10);
        frame.setIsPingResponse(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testParsePingFrame() {
        AbstractHttp2Frame frame = UtilsForTest.frameFromHex(pingFrame);
        Assert.assertTrue(Http2Ping.class.isInstance(frame));

        Http2Ping castFrame = (Http2Ping) frame;
        Assert.assertFalse(castFrame.isPingResponse());
        Assert.assertEquals(castFrame.getOpaqueData(), 0x10);
    }

    @Test
    public void testParsePongFrame() {
        AbstractHttp2Frame frame = UtilsForTest.frameFromHex(pongFrame);
        Assert.assertTrue(Http2Ping.class.isInstance(frame));

        Http2Ping castFrame = (Http2Ping) frame;
        Assert.assertTrue(castFrame.isPingResponse());
        Assert.assertEquals(castFrame.getOpaqueData(), 0x10);
    }
}
