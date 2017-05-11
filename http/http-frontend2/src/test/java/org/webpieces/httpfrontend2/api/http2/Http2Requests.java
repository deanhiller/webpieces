package org.webpieces.httpfrontend2.api.http2;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Http2Requests {
	public static HeaderSettings createSomeSettings() {
		HeaderSettings settings = new HeaderSettings();
		settings.setHeaderTableSize(4099);
		settings.setInitialWindowSize(5009);
		settings.setMaxConcurrentStreams(1L);
		settings.setMaxFrameSize(16385);
		settings.setMaxHeaderListSize(5222);
		settings.setPushEnabled(true);
		return settings;
	}

	public static Http2Headers createRequest(boolean eos) {
		List<Http2Header> headers = new ArrayList<>();
		
	    headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
	    headers.add(new Http2Header(Http2HeaderName.AUTHORITY, "somehost.com"));
	    headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
	    headers.add(new Http2Header(Http2HeaderName.SCHEME, "http"));
	    headers.add(new Http2Header(Http2HeaderName.HOST, "somehost.com"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
	    headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
	    
	    Http2Headers request = new Http2Headers(headers);
	    request.setEndOfStream(eos);
		return request;
	}
	
	public static Http2Headers createResponse() {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "id"));
	    
	    Http2Headers response = new Http2Headers(headers);
	    response.setEndOfStream(true);
	    
		return response;
	}
}
