package org.webpieces.throughput;

import org.webpieces.http2translations.api.Http2ToHttp1_1;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class RequestCreator {

	public static Http2Request createHttp2Request() {
		Http2Request request = new Http2Request();
		request.addHeader(new Http2Header(Http2HeaderName.METHOD, "/"));
		request.addHeader(new Http2Header(Http2HeaderName.PATH, "/"));
		request.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "myhost.com"));
		request.addHeader(new Http2Header(Http2HeaderName.SCHEME, "http"));
		
		return request;
	}

	public static HttpRequest createHttp1_1Request() {
		Http2Request request = createHttp2Request();
		HttpRequest http1Request = Http2ToHttp1_1.translateRequest(request);
		return http1Request;
	}

	public static Http2Response createHttp2Response() {
		Http2Response resp = new Http2Response();
		resp.addHeader(new Http2Header(Http2HeaderName.STATUS, "200"));
		resp.addHeader(new Http2Header("serverid", "3"));
		resp.setEndOfStream(true);
		return resp;
	}

	public static HttpResponse createHttp1_1Response() {
		Http2Response resp = createHttp2Response();
		return Http2ToHttp1_1.translateResponse(resp);
	}

}
