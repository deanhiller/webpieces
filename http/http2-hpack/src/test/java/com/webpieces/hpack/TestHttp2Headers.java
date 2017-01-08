package com.webpieces.hpack;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class TestHttp2Headers {
    private static LinkedList<Http2Header> pushHeaders = new LinkedList<>();
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
        
        pushHeaders.add(new Http2Header(":method", "GET"));
        pushHeaders.add(new Http2Header(":scheme", "https"));
        pushHeaders.add(new Http2Header(":authority", "www.cloudflare.com"));
        pushHeaders.add(new Http2Header(":path", "/"));
    }

	private BufferCreationPool pool = new BufferCreationPool();
    private HpackParser parser = HpackParserFactory.createParser(pool, false);
    		
    @Test
    public void testCreateRequestHeadersFrame() {
    	Http2Headers headers = new Http2Headers(basicRequestHeaders);
    	DataWrapper data = marshal(headers);
    	Http2Msg frame = parse(data);
    	Assert.assertEquals(headers, frame);
    }

    @Test
    public void testCreateResponseHeadersFrame() {
    	Http2Headers headers = new Http2Headers(basicResponseHeaders);
    	headers.setStreamId(99);
    	DataWrapper data = marshal(headers);
    	Http2Msg frame = parse(data);
    	Assert.assertEquals(headers, frame);
    }

    @Test
    public void testPushFrame() {
    	Http2Push push = new Http2Push(pushHeaders);
    	push.setStreamId(98);
    	push.setPromisedStreamId(5);
    	
    	DataWrapper data = marshal(push);
    	Http2Msg frame = parse(data);
    	Assert.assertEquals(push, frame);
    }
    
	private Http2Msg parse(DataWrapper data) {
		UnmarshalState parseState = parser.prepareToUnmarshal(4096, Integer.MAX_VALUE, 4096L);
    	parseState = parser.unmarshal(parseState, data);
    	List<Http2Msg> parsedFrames = parseState.getParsedFrames();
    	Http2Msg frame = parsedFrames.get(0);
		return frame;
	}

	private DataWrapper marshal(PartialStream headers) {
		MarshalState state = parser.prepareToMarshal(Integer.MAX_VALUE, 4096);
    	DataWrapper data = parser.marshal(state, headers);
		return data;
	}

}
