package com.webpieces.http2parser;

import static com.webpieces.http2parser.UtilsForTest.parser;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import org.junit.Test;

import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class TestHttp2PushPromise {
    private static LinkedList<Http2Header> basicRequestHeaders = new LinkedList<>();

    static {
        basicRequestHeaders.add(new Http2Header(":method", "GET"));
        basicRequestHeaders.add(new Http2Header(":scheme", "https"));
        basicRequestHeaders.add(new Http2Header(":authority", "www.cloudflare.com"));
        basicRequestHeaders.add(new Http2Header(":path", "/"));
    }
    private Encoder encoder = new Encoder(4096);

    @Test
    public void testCreatePushPromiseFrame() {
        PushPromiseFrame frame = new PushPromiseFrame();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        frame.setHeaderFragment(parser.serializeHeaders(basicRequestHeaders, encoder, out));
        frame.setPromisedStreamId(5);

        String hexFrame = UtilsForTest.frameToHex(frame);
        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
