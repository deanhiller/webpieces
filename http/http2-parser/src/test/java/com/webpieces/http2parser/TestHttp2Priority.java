package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Priority;

public class TestHttp2Priority {
    static private String priorityFrame =
            "00 00 05" + //length
            "02" + //type
            "00" + //flags
            "00 00 00 00" + // R + streamid
            "80 00 00 04" + // stream dependency
            "05"; // weight

    static private String priorityFrameMSB =
            "00 00 05" + //length
                    "02" + //type
                    "00" + //flags
                    "00 00 00 00" + // R + streamid
                    "80 00 00 04" + // stream dependency
                    "FF"; // weight

    @Test
    public void testCreatePriorityFrame() {
        Http2Priority frame = new Http2Priority();
        frame.setStreamDependency(4);
        frame.setStreamDependencyIsExclusive(true);
        frame.setWeight((short) 0x5);

        String hexFrame = UtilsForTest.frameToHex(frame);
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

    @Test
    public void testCreatePriorityFrameMSB() {
        Http2Priority frame = new Http2Priority();
        frame.setStreamDependency(4);
        frame.setStreamDependencyIsExclusive(true);
        frame.setWeight((short) 0xFF);
        Assert.assertEquals(frame.getWeight(), 255);
        String hexFrame = UtilsForTest.frameToHex(frame);
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(priorityFrameMSB));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testParsePriorityFrameMSB() {
        Http2Frame frame = UtilsForTest.frameFromHex(priorityFrameMSB);
        Assert.assertTrue(Http2Priority.class.isInstance(frame));

        Http2Priority castFrame = (Http2Priority) frame;
        Assert.assertEquals(castFrame.getWeight(), 255);
    }
}
