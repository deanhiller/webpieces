package org.webpieces.throughput;

import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

public class RequestCreator {

	public static Http2Request createHttp2Request() {
		Http2Request request = new Http2Request();
		request.setEndOfStream(true);
		request.addHeader(new Http2Header(Http2HeaderName.METHOD, "/"));
		request.addHeader(new Http2Header(Http2HeaderName.PATH, "/"));
		request.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "myhost.com"));
		request.addHeader(new Http2Header(Http2HeaderName.SCHEME, "http"));
		
		return request;
	}

	public static HttpRequest createHttp1_1Request() {
		Http2Request request = createHttp2Request();
		HttpRequest http1Request = Http2ToHttp11.translateRequest(request);
		return http1Request;
	}

	public static Http2Response createHttp2Response(int streamId) {
		Http2Response resp = new Http2Response();
		resp.addHeader(new Http2Header(Http2HeaderName.STATUS, "200"));
		resp.addHeader(new Http2Header("serverid", "3"));
		resp.setEndOfStream(true);
		resp.setStreamId(streamId);
		return resp;
	}

	public static HttpResponse createHttp1_1Response() {
		Http2Response resp = createHttp2Response(0);
		return Http2ToHttp11.translateResponse(resp);
	}

}
