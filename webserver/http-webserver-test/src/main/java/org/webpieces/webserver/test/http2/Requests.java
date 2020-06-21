package org.webpieces.webserver.test.http2;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

public class Requests {

	public static Http2Request createRequest(String uri, DataWrapper body) {
		Http2Request req = new Http2Request();
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "yourdomain.com"));
		req.addHeader(new Http2Header(Http2HeaderName.SCHEME, "https"));
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, uri));
		req.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		return req;
	}
}
