package com.webpieces.http2parser;

import com.webpieces.http2parser.api.dto.Http2PushPromise;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestHttp2PushPromise {
    private static Map<String, String> basicRequestHeaders = new HashMap<>();
    static {
        basicRequestHeaders.put(":method", "GET");
        basicRequestHeaders.put(":scheme", "https");
        basicRequestHeaders.put(":authority", "www.cloudflare.com");
        basicRequestHeaders.put(":path", "/");
    }

    @Test
    public void testCreatePushPromiseFrame() {
        Http2PushPromise frame = new Http2PushPromise();
        frame.setHeaders(basicRequestHeaders);
        frame.setPromisedStreamId(5);

        String hexFrame = UtilsForTest.frameToHex(frame);
    //  bidi probably doesn't work because the Map doesn't preserve ordering.
    //  TODO: change references to Map<String, String> to List<Header> to ensure
    //  That we are preserving ordering.
    //  UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
