package org.webpieces.httpclient.api;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

public class Requests {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	public static Http2Request createRequest() {
		return createRequest(true);
	}
	
	static Http2Request createRequest(boolean eos) {
		List<Http2Header> headers = new ArrayList<>();
		
	    headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
	    headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
	    headers.add(new Http2Header(Http2HeaderName.SCHEME, "http"));
	    headers.add(new Http2Header(Http2HeaderName.AUTHORITY, "somehost.com"));
	    headers.add(new Http2Header("serverid", "1"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
	    headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
	    
	    Http2Request request = new Http2Request(headers);
	    request.setEndOfStream(eos);
		return request;
	}
	
	public static Http2Response createResponse(int id, int contentLength) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.STATUS, "200"));
	    headers.add(new Http2Header(Http2HeaderName.SERVER, id+""));
	    headers.add(new Http2Header(Http2HeaderName.CONTENT_LENGTH, contentLength+""));
	    
	    Http2Response response = new Http2Response(headers);
	    if(contentLength == 0)
	    	response.setEndOfStream(true);
	    
		return response;
	}

	public static HttpData createHttpData(int size) {
		
		String s = "";
		for(int i = 0; i < size; i++) {
			s+="h";
		}
		DataWrapper dataWrapper = dataGen.wrapByteArray(s.getBytes());
		return new HttpData(dataWrapper, true);
	}

	public static HttpChunk createHttpChunk(int size) {
		
		String s = "";
		for(int i = 0; i < size; i++) {
			s+="h";
		}
		DataWrapper dataWrapper = dataGen.wrapByteArray(s.getBytes());
		return new HttpChunk(dataWrapper);
	}
	
	public static Http2Response createChunkedResponse(int id) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.STATUS, "200"));
	    headers.add(new Http2Header(Http2HeaderName.SERVER, id+""));
	    
	    Http2Response response = new Http2Response(headers);
	    response.setEndOfStream(false);
	    
		return response;
	}
}
