package com.webpieces.http2parser;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import org.junit.Test;

import java.util.LinkedList;

import static com.webpieces.http2parser.UtilsForTest.parser;

public class TestHttp2PushPromise {
    private static LinkedList<HasHeaderFragment.Header> basicRequestHeaders = new LinkedList<>();

    static {
        basicRequestHeaders.add(new HasHeaderFragment.Header(":method", "GET"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":scheme", "https"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":authority", "www.cloudflare.com"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":path", "/"));
    }

    @Test
    public void testCreatePushPromiseFrame() {
        Http2PushPromise frame = new Http2PushPromise();
        frame.setHeaderFragment(parser.serializeHeaders(basicRequestHeaders));
        frame.setPromisedStreamId(5);

        String hexFrame = UtilsForTest.frameToHex(frame);
        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
