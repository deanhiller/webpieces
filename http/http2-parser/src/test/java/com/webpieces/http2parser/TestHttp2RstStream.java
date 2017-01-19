package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class TestHttp2RstStream {
    private static String connectError =
            "00 00 04" + // length
            "03" + // type
            "00" + // flags
            "00 00 00 04" + // streamid
            "00 00 00 0A"; // payload
    @Test
    public void testCreateRstStream() {
        RstStreamFrame frame = new RstStreamFrame();
        frame.setStreamId(0x4);
        frame.setKnownErrorCode(Http2ErrorCode.CONNECT_ERROR);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test public void testParseRstStream() {
        Http2Frame frame = UtilsForTest.frameFromHex(connectError);
        Assert.assertTrue(RstStreamFrame.class.isInstance(frame));
        RstStreamFrame castFrame = (RstStreamFrame) frame;

        Assert.assertEquals(castFrame.getStreamId(), 4);
        Assert.assertEquals(castFrame.getKnownErrorCode(), Http2ErrorCode.CONNECT_ERROR);
    }
}
