package com.webpieces.http2parser;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class TestHttp2Settings {
    static private String basicSettings =
            "00 00 0C" + // length
            "04" +  // type
            "00" + //flags
            "00 00 00 00" + //R + streamid
            "00 02 00 00 00 01" + //setting 1 (enable push)
            "00 03 00 00 01 00"; //setting 2 (max streams)

    static private String ackFrame =
            "00 00 00" + // length
            "04" +  // type
            "01" + // flags (ack)
            "00 00 00 00"; // R + streamid

    @Test
    public void testCreateSettings() {
        SettingsFrame frame = new SettingsFrame();
        frame.addSetting(new Http2Setting(SettingsParameter.SETTINGS_ENABLE_PUSH, 1L));
        frame.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256L));
        String hexFrame = UtilsForTest.frameToHex(frame);

        // We can't test bidi for settings frames because the order in which
        // settings show up is non-deterministic.
        // we'll just parse it and make sure that the settings we set are set.

        Http2Frame parsedFrame = UtilsForTest.frameFromHex(hexFrame);
        Assert.assertTrue(SettingsFrame.class.isInstance(frame));
        SettingsFrame castFrame = (SettingsFrame) parsedFrame;
        
        List<Http2Setting> settings = castFrame.getSettings();
        Assert.assertEquals(2, settings.size());
        
        Http2Setting setting1 = settings.get(0);
        Assert.assertEquals(SettingsParameter.SETTINGS_ENABLE_PUSH, setting1.getKnownName());
        Assert.assertEquals(1L, setting1.getValue());
        
        Http2Setting setting2 = settings.get(1);
        Assert.assertEquals(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, setting2.getKnownName());
        Assert.assertEquals(256L, setting2.getValue());        
    }

    @Test
    public void testParseSettings() {
        Http2Frame frame = UtilsForTest.frameFromHex(basicSettings);
        Assert.assertTrue(SettingsFrame.class.isInstance(frame));

        SettingsFrame castFrame = (SettingsFrame) frame;
        List<Http2Setting> settings = castFrame.getSettings();
        Assert.assertEquals(2, settings.size());
        
        Http2Setting setting1 = settings.get(0);
        Assert.assertEquals(SettingsParameter.SETTINGS_ENABLE_PUSH, setting1.getKnownName());
        Assert.assertEquals(1L, setting1.getValue());
        
        Http2Setting setting2 = settings.get(1);
        Assert.assertEquals(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, setting2.getKnownName());
        Assert.assertEquals(256L, setting2.getValue());   
    }

    @Test
    public void testParseAck() {
        Http2Frame frame = UtilsForTest.frameFromHex(ackFrame);
        Assert.assertTrue(SettingsFrame.class.isInstance(frame));

        SettingsFrame castFrame = (SettingsFrame) frame;
        Assert.assertTrue(castFrame.isAck());
        Assert.assertEquals(castFrame.getSettings().size(), 0);
    }
    @Test
    public void testCreateAck() {
        SettingsFrame frame = new SettingsFrame();
        frame.setAck(true);
        frame.addSetting(new Http2Setting(SettingsParameter.SETTINGS_ENABLE_PUSH, 1L));
        frame.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256L));
        
        String hexFrame = UtilsForTest.frameToHex(frame);
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(ackFrame));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
