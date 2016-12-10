package com.webpieces.http2parser;

import static com.webpieces.http2parser.UtilsForTest.parser;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import org.junit.Test;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Headers;

public class TestHttp2Headers {
    private static LinkedList<HasHeaderFragment.Header> basicRequestHeaders = new LinkedList<>();
    private static LinkedList<HasHeaderFragment.Header> basicResponseHeaders = new LinkedList<>();
    static {
        basicRequestHeaders.add(new HasHeaderFragment.Header(":method", "GET"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":scheme", "https"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":authority", "www.cloudflare.com"));
        basicRequestHeaders.add(new HasHeaderFragment.Header(":path", "/"));

        basicResponseHeaders.add(new HasHeaderFragment.Header(":status", "200"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("date", "Tue, 27 Sep 2016 19:41:50 GMT"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("content-type", "text/html"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("set-cookie", "__cfduid=d8bfe297ef26ef6252ea3a822360a6f411475005310; expires=Wed, 27-Sep-17 19:41:50 GMT; path=/; domain=.cloudflare.com; HttpOnly"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("last-modified", "Tue, 27 Sep 2016 17:39:01 GMT"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("cache-control", "public, max-age=14400"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("served-in-seconds", "0.001"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("cf-cache-status", "REVALIDATED"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("expires", "Tue, 27 Sep 2016 23:41:50 GMT"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("server", "cloudflare-nginx"));
        basicResponseHeaders.add(new HasHeaderFragment.Header("cf-ray", "2e916f776c724fd5-DEN"));
    }

    private Decoder decoder = new Decoder(4096, 4096);
    private Encoder encoder = new Encoder(4096);
    @Test
    public void testCreateRequestHeadersFrame() {
        Http2Headers frame = new Http2Headers();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        frame.setHeaderFragment(parser.serializeHeaders(basicRequestHeaders, encoder, out));
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    @Test
    public void testCreateResponseHeadersFrame() {
        Http2Headers frame = new Http2Headers();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        frame.setHeaderFragment(parser.serializeHeaders(basicResponseHeaders, encoder, out));
        frame.setEndHeaders(true);
        String hexFrame = UtilsForTest.frameToHex(frame);

        UtilsForTest.testBidiFromBytes(hexFrame);
    }

    // TODO: Add bits -> frame tests from real world snooped data
}
