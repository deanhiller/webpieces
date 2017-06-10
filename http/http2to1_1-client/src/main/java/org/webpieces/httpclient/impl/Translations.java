package org.webpieces.httpclient.impl;

import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclient.api.HttpFullRequest;
import org.webpieces.httpclient.api.HttpFullResponse;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class Translations {

	public static HttpData translate(StreamMsg data) {
		return null;
	}

	public static DataFrame translate(HttpData chunk) {
		return null;
	}
	
	public static HttpFullRequest translate(FullRequest request) {
		return null;
	}

	public static FullResponse translate(HttpFullResponse r) {
		return null;
	}

	public static HttpRequest translate(Http2Request request) {
		return null;
	}

	public static Http2Response translate(HttpResponse resp) {
		return null;
	}

}
