package com.webpieces.http2parser;

import com.webpieces.http2parser.api.Http2Frame;
import com.webpieces.http2parser.api.Http2Priority;
import com.webpieces.http2parser.impl.Http2PriorityImpl;
import org.junit.Assert;
import org.junit.Test;

public class TestHttp2Priority {
    static private String priorityFrame =
            "00 00 05" + //length
            "02" + //type
            "00" + //flags
            "00 00 00 00" + // R + streamid
            "80 00 00 04" + // stream dependency
            "05"; // weight

    @Test
    public void testCreatePriorityFrame() {
        Http2Priority frame = new Http2PriorityImpl();
        frame.setStreamDependency(4);
        frame.setStreamDependencyIsExclusive();
        frame.setWeight((byte) 0x5);

        String hexFrame = UtilsForTest.toHexString(frame.getBytes());
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(priorityFrame));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testParsePriorityFrame() {
        Http2Frame frame = UtilsForTest.frameFromHex(priorityFrame);
        Assert.assertTrue(Http2Priority.class.isInstance(frame));

        Http2Priority castFrame = (Http2Priority) frame;

        Assert.assertEquals(castFrame.getStreamDependency(), 4);
        Assert.assertTrue(castFrame.isStreamDependencyIsExclusive());
    }
}
