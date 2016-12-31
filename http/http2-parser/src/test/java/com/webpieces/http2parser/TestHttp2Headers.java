package com.webpieces.http2parser;

import static com.webpieces.http2parser.UtilsForTest.parser;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import org.junit.Test;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class TestHttp2Headers {
    private static LinkedList<Http2Header> basicRequestHeaders = new LinkedList<>();
    private static LinkedList<Http2Header> basicResponseHeaders = new LinkedList<>();
    static {
        basicRequestHeaders.add(new Http2Header(":method", "GET"));
        basicRequestHeaders.add(new Http2Header(":scheme", "https"));
        basicRequestHeaders.add(new Http2Header(":authority", "www.cloudflare.com"));
        basicRequestHeaders.add(new Http2Header(":path", "/"));

        basicResponseHeaders.add(new Http2Header(":status", "200"));
        basicResponseHeaders.add(new Http2Header("date", "Tue, 27 Sep 2016 19:41:50 GMT"));
        basicResponseHeaders.add(new Http2Header("content-type", "text/html"));
        basicResponseHeaders.add(new Http2Header("set-cookie", "__cfduid=d8bfe297ef26ef6252ea3a822360a6f411475005310; expires=Wed, 27-Sep-17 19:41:50 GMT; path=/; domain=.cloudflare.com; HttpOnly"));
        basicResponseHeaders.add(new Http2Header("last-modified", "Tue, 27 Sep 2016 17:39:01 GMT"));
        basicResponseHeaders.add(new Http2Header("cache-control", "public, max-age=14400"));
        basicResponseHeaders.add(new Http2Header("served-in-seconds", "0.001"));
        basicResponseHeaders.add(new Http2Header("cf-cache-status", "REVALIDATED"));
        basicResponseHeaders.add(new Http2Header("expires", "Tue, 27 Sep 2016 23:41:50 GMT"));
        basicResponseHeaders.add(new Http2Header("server", "cloudflare-nginx"));
        basicResponseHeaders.add(new Http2Header("cf-ray", "2e916f776c724fd5-DEN"));
    }

    private Decoder decoder = new Decoder(4096, 4096);
    private Encoder encoder = new Encoder(4096);
    @Test
    public void testCreateRequestHeadersFrame() {
        HeadersFrame frame = new HeadersFrame();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        frame.setHeaderFragment(parser.serializeHeaders(basicRequestHeaders, encoder, out));
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testCreateResponseHeadersFrame() {
        HeadersFrame frame = new HeadersFrame();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        frame.setHeaderFragment(parser.serializeHeaders(basicResponseHeaders, encoder, out));
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    // TODO: Add bits -> frame tests from real world snooped data
}
