package org.webpieces.httpfrontend2.api;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Responses {
	
	public static Http2Headers createResponse(int id) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "id"));
	    
	    Http2Headers response = new Http2Headers(headers);
	    response.setEndOfStream(true);
	    
		return response;
	}
	
//    public static HttpResponse createResponse(KnownStatusCode status, DataWrapper body) {
//        HttpResponse resp = new HttpResponse();
//        HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
//        HttpResponseStatus statusCode = new HttpResponseStatus();
//        statusCode.setKnownStatus(status);
//        statusLine.setStatus(statusCode);
//        resp.setStatusLine(statusLine);
//
//        resp.setBody(body);
//        resp.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, Integer.toString(body.getReadableSize())));
//        return resp;
//    }
//
//    public static HttpResponse copyResponseExceptBody(HttpResponse response) {
//        HttpResponse newResponse = new HttpResponse();
//        newResponse.setStatusLine(response.getStatusLine());
//        response.getHeaders().forEach(newResponse::addHeader);
//        return newResponse;
//    }
}
