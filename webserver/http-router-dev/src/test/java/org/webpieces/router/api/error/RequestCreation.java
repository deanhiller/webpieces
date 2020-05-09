package org.webpieces.router.api.error;

import org.webpieces.ctx.api.HttpMethod;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class RequestCreation {
	public static Http2Request createHttpRequest(HttpMethod method, String path) {
		Http2Request req = new Http2Request();
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, method.getCode()));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, path));
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "orderly.com"));
		return req;
	}
}
