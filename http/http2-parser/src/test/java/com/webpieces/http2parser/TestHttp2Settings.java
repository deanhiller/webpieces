package com.webpieces.http2parser;

import com.webpieces.http2parser.api.Http2Frame;
import com.webpieces.http2parser.api.Http2Settings;
import com.webpieces.http2parser.impl.Http2SettingsImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

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
        Http2Settings frame = new Http2SettingsImpl();
        frame.setSetting(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH, 1);
        frame.setSetting(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256);
        String hexFrame = UtilsForTest.toHexString(frame.getBytes());
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(basicSettings));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testParseSettings() {
        Http2Frame frame = UtilsForTest.frameFromHex(basicSettings);
        Assert.assertTrue(Http2Settings.class.isInstance(frame));

        Http2Settings castFrame = (Http2Settings) frame;
        Map<Http2Settings.Parameter, Integer> settings = castFrame.getSettings();
        Assert.assertEquals(settings.get(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH).longValue(), 1);
        Assert.assertEquals(settings.get(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS).longValue(), 256);
        Assert.assertFalse(settings.containsKey(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE));
    }

    @Test
    public void testParseAck() {
        Http2Frame frame = UtilsForTest.frameFromHex(ackFrame);
        Assert.assertTrue(Http2Settings.class.isInstance(frame));

        Http2Settings castFrame = (Http2Settings) frame;
        Assert.assertTrue(castFrame.isAck());
        Assert.assertEquals(castFrame.getSettings().size(), 0);
    }
    @Test
    public void testCreateAck() {
        Http2Settings frame = new Http2SettingsImpl();
        frame.setAck();
        frame.setSetting(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH, 1);
        frame.setSetting(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256);

        // If it's an ack there's no settings, even if we set them
        Assert.assertEquals(frame.getSettings().size(), 0);

        String hexFrame = UtilsForTest.toHexString(frame.getBytes());
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(ackFrame));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
