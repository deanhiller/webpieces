package com.webpieces.http2parser;

import com.webpieces.http2parser.api.Http2PushPromise;
import com.webpieces.http2parser.impl.Http2PushPromiseImpl;
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
        Http2PushPromise frame = new Http2PushPromiseImpl();
        frame.setHeaders(basicRequestHeaders);
        frame.setPromisedStreamId(5);

        String hexFrame = UtilsForTest.toHexString(frame.getBytes());

        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
