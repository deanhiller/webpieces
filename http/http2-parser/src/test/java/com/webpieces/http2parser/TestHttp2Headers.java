package com.webpieces.http2parser;

import com.webpieces.http2parser.api.dto.Http2Headers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestHttp2Headers {
    private static Map<String, String> basicRequestHeaders = new HashMap<>();
    private static Map<String, String> basicResponseHeaders = new HashMap<>();
    static {
        basicRequestHeaders.put(":method", "GET");
        basicRequestHeaders.put(":scheme", "https");
        basicRequestHeaders.put(":authority", "www.cloudflare.com");
        basicRequestHeaders.put(":path", "/");

        basicResponseHeaders.put(":status", "200");
        basicResponseHeaders.put("date", "Tue, 27 Sep 2016 19:41:50 GMT");
        basicResponseHeaders.put("content-type", "text/html");
        basicResponseHeaders.put("set-cookie", "__cfduid=d8bfe297ef26ef6252ea3a822360a6f411475005310; expires=Wed, 27-Sep-17 19:41:50 GMT; path=/; domain=.cloudflare.com; HttpOnly");
        basicResponseHeaders.put("last-modified", "Tue, 27 Sep 2016 17:39:01 GMT");
        basicResponseHeaders.put("cache-control", "public, max-age=14400");
        basicResponseHeaders.put("served-in-seconds", "0.001");
        basicResponseHeaders.put("cf-cache-status", "REVALIDATED");
        basicResponseHeaders.put("expires", "Tue, 27 Sep 2016 23:41:50 GMT");
        basicResponseHeaders.put("server", "cloudflare-nginx");
        basicResponseHeaders.put("cf-ray", "2e916f776c724fd5-DEN");
    }

    @Test
    public void testCreateRequestHeadersFrame() {
        Http2Headers frame = new Http2Headers();
        frame.setHeaders(basicRequestHeaders);
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testCreateResponseHeadersFrame() {
        Http2Headers frame = new Http2Headers();
        frame.setHeaders(basicResponseHeaders);
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    // TODO: Add bits -> frame tests from real world snooped data
}
