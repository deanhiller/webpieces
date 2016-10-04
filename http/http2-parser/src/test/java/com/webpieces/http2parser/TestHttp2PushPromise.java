package com.webpieces.http2parser;

import com.webpieces.http2parser.api.dto.HasHeaders;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.webpieces.http2parser.UtilsForTest.parser;

public class TestHttp2PushPromise {
    private static LinkedList<HasHeaders.Header> basicRequestHeaders = new LinkedList<>();

    static {
        basicRequestHeaders.add(new HasHeaders.Header(":method", "GET"));
        basicRequestHeaders.add(new HasHeaders.Header(":scheme", "https"));
        basicRequestHeaders.add(new HasHeaders.Header(":authority", "www.cloudflare.com"));
        basicRequestHeaders.add(new HasHeaders.Header(":path", "/"));
    }

    @Test
    public void testCreatePushPromiseFrame() {
        Http2PushPromise frame = new Http2PushPromise();
        frame.setHeaders(basicRequestHeaders);
        frame.setSerializedHeaders(parser.createSerializedHeaders(basicRequestHeaders));
        frame.setPromisedStreamId(5);

        String hexFrame = UtilsForTest.frameToHex(frame);
        UtilsForTest.testBidiFromBytes(hexFrame);
    }
}
