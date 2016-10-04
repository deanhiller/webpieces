package com.webpieces.http2parser;

import com.webpieces.http2parser.api.dto.HasHeaders;
import com.webpieces.http2parser.api.dto.Http2Headers;
import org.junit.Test;

import java.util.LinkedList;

import static com.webpieces.http2parser.UtilsForTest.parser;

public class TestHttp2Headers {
    private static LinkedList<HasHeaders.Header> basicRequestHeaders = new LinkedList<>();
    private static LinkedList<HasHeaders.Header> basicResponseHeaders = new LinkedList<>();
    static {
        basicRequestHeaders.add(new HasHeaders.Header(":method", "GET"));
        basicRequestHeaders.add(new HasHeaders.Header(":scheme", "https"));
        basicRequestHeaders.add(new HasHeaders.Header(":authority", "www.cloudflare.com"));
        basicRequestHeaders.add(new HasHeaders.Header(":path", "/"));

        basicResponseHeaders.add(new HasHeaders.Header(":status", "200"));
        basicResponseHeaders.add(new HasHeaders.Header("date", "Tue, 27 Sep 2016 19:41:50 GMT"));
        basicResponseHeaders.add(new HasHeaders.Header("content-type", "text/html"));
        basicResponseHeaders.add(new HasHeaders.Header("set-cookie", "__cfduid=d8bfe297ef26ef6252ea3a822360a6f411475005310; expires=Wed, 27-Sep-17 19:41:50 GMT; path=/; domain=.cloudflare.com; HttpOnly"));
        basicResponseHeaders.add(new HasHeaders.Header("last-modified", "Tue, 27 Sep 2016 17:39:01 GMT"));
        basicResponseHeaders.add(new HasHeaders.Header("cache-control", "public, max-age=14400"));
        basicResponseHeaders.add(new HasHeaders.Header("served-in-seconds", "0.001"));
        basicResponseHeaders.add(new HasHeaders.Header("cf-cache-status", "REVALIDATED"));
        basicResponseHeaders.add(new HasHeaders.Header("expires", "Tue, 27 Sep 2016 23:41:50 GMT"));
        basicResponseHeaders.add(new HasHeaders.Header("server", "cloudflare-nginx"));
        basicResponseHeaders.add(new HasHeaders.Header("cf-ray", "2e916f776c724fd5-DEN"));
    }

    @Test
    public void testCreateRequestHeadersFrame() {
        Http2Headers frame = new Http2Headers();
        frame.setHeaders(basicRequestHeaders);
        frame.setSerializedHeaders(parser.serializedHeaders(basicRequestHeaders));
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testCreateResponseHeadersFrame() {
        Http2Headers frame = new Http2Headers();
        frame.setHeaders(basicResponseHeaders);
        frame.setSerializedHeaders(parser.serializedHeaders(basicResponseHeaders));
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    // TODO: Add bits -> frame tests from real world snooped data
}
