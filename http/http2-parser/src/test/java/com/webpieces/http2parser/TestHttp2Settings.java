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
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class TestHttp2Settings {
    private static Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());

    static private String basicSettings() {
    	String data =
            "00 00 0C" + // length
            "04" +  // type
            "00" + //flags
            "00 00 00 00" + //R + streamid
            "00 02 00 00 00 01" + //setting 1 (enable push)
            "00 03 00 00 01 00"; //setting 2 (max streams)
        return data.replaceAll("\\s+","");
    }
    
    static private String ackFrame() {
    	String data =
            "00 00 00" + // length
            "04" +  // type
            "01" + // flags (ack)
            "00 00 00 00"; // R + streamid
        return data.replaceAll("\\s+","");
    }
    
	private Http2Memento memento;

    @Before
    public void setUp() {
    	memento = parser.prepareToParse(Long.MAX_VALUE);    	
    }
    
    @Test
    public void testParseSettings() {
    	DataWrapper data = Util.hexToBytes(basicSettings());
    	parser.parse(memento, data);
    	
    	SettingsFrame frame = (SettingsFrame) assertGood();
    	Assert.assertEquals(0, frame.getStreamId());
        Assert.assertFalse(frame.isAck());
        Assert.assertEquals(frame.getSettings().size(), 2);
        Http2Setting setting = frame.getSettings().get(0);
        //first setting must be push from order in the bytes
        Assert.assertEquals(SettingsParameter.SETTINGS_ENABLE_PUSH, setting.getKnownName());
        Assert.assertEquals(1, setting.getValue());        
    }
    
    @Test
    public void testParseAck() {
    	DataWrapper data = Util.hexToBytes(ackFrame());
    	parser.parse(memento, data);
    	
    	SettingsFrame frame = (SettingsFrame) assertGood();
    	Assert.assertEquals(0, frame.getStreamId());
        Assert.assertTrue(frame.isAck());
        Assert.assertEquals(frame.getSettings().size(), 0);
    }
	private Http2Frame assertGood() {
		Assert.assertEquals(0, memento.getLeftOverData().getReadableSize());
    	List<Http2Frame> frames = memento.getParsedFrames();
    	Assert.assertEquals(1, frames.size());
    	return frames.get(0);
	}
	
    @Test
    public void testMarshalSettings() {
        SettingsFrame frame = new SettingsFrame();
        frame.addSetting(new Http2Setting(SettingsParameter.SETTINGS_ENABLE_PUSH, 1L));
        frame.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256L));

        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(basicSettings(), hexFrame);
    }
    
    @Test
    public void testMarshalAck() {
        SettingsFrame frame = new SettingsFrame();
        frame.setAck(true);
        
        byte[] data = parser.marshal(frame).createByteArray();
        String hexFrame = Util.toHexString(data);
        Assert.assertEquals(ackFrame(), hexFrame);
    }
}
