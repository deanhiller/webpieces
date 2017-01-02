package com.webpieces.http2parser;

import java.util.LinkedList;

import org.junit.Test;

import com.twitter.hpack.Encoder;
import com.webpieces.http2engine.impl.HeaderEncoding;
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
    private HeaderEncoding encoding = new HeaderEncoding(new Encoder(4096), Integer.MAX_VALUE);

    @Test
    public void testCreatePushPromiseFrame() {
        PushPromiseFrame frame = new PushPromiseFrame();
        frame.setHeaderFragment(encoding.serializeHeaders(basicRequestHeaders));
        frame.setPromisedStreamId(5);

        String hexFrame = UtilsForTest.frameToHex(frame);
        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
