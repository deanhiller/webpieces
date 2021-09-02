package org.webpieces.webserver.test.http2;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

public class Requests {

	private static DataWrapperGenerator gen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	public static Http2Request createBaseRequest(String method, String scheme, String path) {
		Http2Request req = new Http2Request();
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "yourdomain.com"));
		req.addHeader(new Http2Header(Http2HeaderName.SCHEME,  scheme));
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, method));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, path));

		return req;
	}

	public static Http2Request createRequest(String uri, DataWrapper body) {
		Http2Request req = new Http2Request();
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "yourdomain.com"));
		req.addHeader(new Http2Header(Http2HeaderName.SCHEME, "https"));
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, uri));
		req.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		return req;
	}

	public static FullRequest createJsonRequest(String method, String url, String json) {
		Http2Request req = createBaseRequest(method, "https", url);

		DataWrapper body = gen.wrapByteArray(json.getBytes());
		req.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, body.getReadableSize()+""));

		return new FullRequest(req, body, null);
	}
}
