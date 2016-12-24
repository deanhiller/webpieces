package com.webpieces.http2parser2;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;

import com.webpieces.http2parser.UtilsForTest;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.Http2Priority;

public class TestHttp2Priority {
    private static Http2Parser2 parser = Http2ParserFactory.createParser2(new BufferCreationPool());

    static private String priorityFrame() {
           String priorityFrame = 
        	"00 00 05" + //length
            "02" + //type
            "00" + //flags
            "00 00 00 00" + // R + streamid
            "80 00 00 04" + // stream dependency
            "05"; // weight
           return priorityFrame.replaceAll("\\s+","");
    }

    static private String priorityFrameMSB() {
    	String priorityMSB =
            "00 00 05" + //length
                    "02" + //type
                    "00" + //flags
                    "00 00 00 00" + // R + streamid
                    "80 00 00 04" + // stream dependency
                    "FF"; // weight
    	return priorityMSB.replaceAll("\\s+","");
    }

    @Test
    public void testCreatePriorityFrame() {
        Http2Priority frame = new Http2Priority();
        frame.setStreamDependency(4);
        frame.setStreamDependencyIsExclusive(true);
        frame.setWeight((short) 0x5);

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(priorityFrame(), hexFrame);
    }

//    @Test
//    public void testParsePriorityFrame() {
//        Http2Frame frame = UtilsForTest.frameFromHex(priorityFrame);
//        Assert.assertTrue(Http2Priority.class.isInstance(frame));
//
//        Http2Priority castFrame = (Http2Priority) frame;
//
//        Assert.assertEquals(castFrame.getStreamDependency(), 4);
//        Assert.assertTrue(castFrame.isStreamDependencyIsExclusive());
//    }
//
    @Test
    public void testCreatePriorityFrameMSB() {
        Http2Priority frame = new Http2Priority();
        frame.setStreamDependency(4);
        frame.setStreamDependencyIsExclusive(true);
        frame.setWeight((short) 0xFF);
        Assert.assertEquals(frame.getWeight(), 255);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = UtilsForTest.toHexString(data);
        Assert.assertEquals(priorityFrameMSB(), hexFrame);
    }
//
//    @Test
//    public void testParsePriorityFrameMSB() {
//        Http2Frame frame = UtilsForTest.frameFromHex(priorityFrameMSB);
//        Assert.assertTrue(Http2Priority.class.isInstance(frame));
//
//        Http2Priority castFrame = (Http2Priority) frame;
//        Assert.assertEquals(castFrame.getWeight(), 255);
//    }
}
