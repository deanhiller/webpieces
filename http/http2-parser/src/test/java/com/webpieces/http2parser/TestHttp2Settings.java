package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2Settings;

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
        Http2Settings frame = new Http2Settings();
        frame.setSetting(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH, 1L);
        frame.setSetting(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256L);
        String hexFrame = UtilsForTest.frameToHex(frame);

        // We can't test bidi for settings frames because the order in which
        // settings show up is non-deterministic.
        // we'll just parse it and make sure that the settings we set are set.

        AbstractHttp2Frame parsedFrame = UtilsForTest.frameFromHex(hexFrame);
        Assert.assertTrue(Http2Settings.class.isInstance(frame));
        Http2Settings castFrame = (Http2Settings) parsedFrame;
        Assert.assertEquals(1L, castFrame.getSettings().get(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH).longValue());
        Assert.assertEquals(256L, castFrame.getSettings().get(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS).longValue());
        Assert.assertNull(castFrame.getSettings().get(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE));
        Assert.assertNull(castFrame.getSettings().get(Http2Settings.Parameter.SETTINGS_INITIAL_WINDOW_SIZE));
        Assert.assertNull(castFrame.getSettings().get(Http2Settings.Parameter.SETTINGS_HEADER_TABLE_SIZE));
        Assert.assertNull(castFrame.getSettings().get(Http2Settings.Parameter.SETTINGS_MAX_HEADER_LIST_SIZE));
    }

    @Test
    public void testParseSettings() {
        AbstractHttp2Frame frame = UtilsForTest.frameFromHex(basicSettings);
        Assert.assertTrue(Http2Settings.class.isInstance(frame));

        Http2Settings castFrame = (Http2Settings) frame;
        Http2SettingsMap settings = castFrame.getSettings();
        Assert.assertEquals(settings.get(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH).longValue(), 1);
        Assert.assertEquals(settings.get(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS).longValue(), 256);
        Assert.assertFalse(settings.containsKey(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE));
    }

    @Test
    public void testParseAck() {
        AbstractHttp2Frame frame = UtilsForTest.frameFromHex(ackFrame);
        Assert.assertTrue(Http2Settings.class.isInstance(frame));

        Http2Settings castFrame = (Http2Settings) frame;
        Assert.assertTrue(castFrame.isAck());
        Assert.assertEquals(castFrame.getSettings().size(), 0);
    }
    @Test
    public void testCreateAck() {
        Http2Settings frame = new Http2Settings();
        frame.setAck(true);
        frame.setSetting(Http2Settings.Parameter.SETTINGS_ENABLE_PUSH, 1L);
        frame.setSetting(Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS, 256L);

        // If it's an ack there's no settings, even if we set them
        Assert.assertEquals(frame.getSettings().size(), 0);

        String hexFrame = UtilsForTest.frameToHex(frame);
        Assert.assertArrayEquals(UtilsForTest.toByteArray(hexFrame), UtilsForTest.toByteArray(ackFrame));

        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
