package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

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
        PriorityFrame frame = new PriorityFrame();
        PriorityDetails details = frame.getPriorityDetails();
        details.setStreamDependency(4);
        details.setStreamDependencyIsExclusive(true);
        details.setWeight((short) 0x5);

        String hexFrame = UtilsForTest.frameToHex(frame);
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(priorityFrame));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testParsePriorityFrame() {
        Http2Frame frame = UtilsForTest.frameFromHex(priorityFrame);
        Assert.assertTrue(PriorityFrame.class.isInstance(frame));

        PriorityFrame castFrame = (PriorityFrame) frame;

        PriorityDetails details = castFrame.getPriorityDetails();

        Assert.assertEquals(details.getStreamDependency(), 4);
        Assert.assertTrue(details.isStreamDependencyIsExclusive());
    }

    @Test
    public void testCreatePriorityFrameMSB() {
        PriorityFrame frame = new PriorityFrame();
        PriorityDetails details = frame.getPriorityDetails();

        details.setStreamDependency(4);
        details.setStreamDependencyIsExclusive(true);
        details.setWeight((short) 0xFF);
        Assert.assertEquals(details.getWeight(), 255);
        String hexFrame = UtilsForTest.frameToHex(frame);
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(priorityFrameMSB));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testParsePriorityFrameMSB() {
        Http2Frame frame = UtilsForTest.frameFromHex(priorityFrameMSB);
        Assert.assertTrue(PriorityFrame.class.isInstance(frame));

        PriorityFrame castFrame = (PriorityFrame) frame;
        PriorityDetails details = castFrame.getPriorityDetails();
        Assert.assertEquals(details.getWeight(), 255);
    }
}
