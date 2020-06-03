package com.webpieces.http2parser;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.TwoPools;

import com.webpieces.http2.api.dto.lowlevel.PriorityFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.PriorityDetails;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestHttp2Priority {
    private static Http2Parser parser = Http2ParserFactory.createParser(new TwoPools("pl", new SimpleMeterRegistry()));

    static private String priorityFrame() {
           String priorityFrame = 
        	"00 00 05" + //length
            "02" + //type
            "00" + //flags
            "00 00 00 01" + // R + streamid
            "80 00 00 04" + // stream dependency
            "05"; // weight
           return priorityFrame.replaceAll("\\s+","");
    }

    static private String priorityFrameMSB() {
    	String priorityMSB =
            "00 00 05" + //length
                    "02" + //type
                    "00" + //flags
                    "00 00 00 01" + // R + streamid
                    "80 00 00 04" + // stream dependency
                    "FF"; // weight
    	return priorityMSB.replaceAll("\\s+","");
    }

	private Http2Memento memento;

    @Before
    public void setUp() {
    	memento = parser.prepareToParse(Long.MAX_VALUE);    	
    }
    
    @Test
    public void testParsePriorityFrame() {
    	DataWrapper data = Util.hexToBytes(priorityFrame());
    	parser.parse(memento, data);
    	
    	PriorityFrame frame = (PriorityFrame) assertGood();
    	Assert.assertEquals(1, frame.getStreamId());
    	PriorityDetails details = frame.getPriorityDetails();
    	Assert.assertTrue(details.isStreamDependencyIsExclusive());
    	Assert.assertEquals(5, details.getWeight());
    	Assert.assertEquals(4, details.getStreamDependency());
    }
    
    @Test
    public void testParsePriorityFrameMSB() {
    	DataWrapper data = Util.hexToBytes(priorityFrameMSB());
    	parser.parse(memento, data);
    	
    	PriorityFrame frame = (PriorityFrame) assertGood();
    	Assert.assertEquals(1, frame.getStreamId());
    	PriorityDetails details = frame.getPriorityDetails();
    	Assert.assertTrue(details.isStreamDependencyIsExclusive());
    	Assert.assertEquals(255, details.getWeight());
    	Assert.assertEquals(4, details.getStreamDependency());
    }
    
	private Http2Frame assertGood() {
		Assert.assertEquals(0, memento.getLeftOverDataSize());
    	List<Http2Frame> frames = memento.getParsedFrames();
    	Assert.assertEquals(1, frames.size());
    	return frames.get(0);
	}
	
    @Test
    public void testMarshalPriorityFrame() {
        PriorityFrame frame = new PriorityFrame();
        frame.setStreamId(1);
        PriorityDetails details = frame.getPriorityDetails();
        details.setStreamDependency(4);
        details.setStreamDependencyIsExclusive(true);
        details.setWeight((short) 0x5);

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(priorityFrame(), hexFrame);
    }
	
    @Test
    public void testMarshalPriorityFrameMSB() {
        PriorityFrame frame = new PriorityFrame();
        frame.setStreamId(1);
        PriorityDetails details = frame.getPriorityDetails();
        details.setStreamDependency(4);
        details.setStreamDependencyIsExclusive(true);
        details.setWeight((short) 0xFF);
        Assert.assertEquals(details.getWeight(), 255);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(priorityFrameMSB(), hexFrame);
    }

}
