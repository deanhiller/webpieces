package org.webpieces.http2translations.api;

import java.util.HashSet;
import java.util.Set;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpRequestMethod;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownStatusCode;

import com.webpieces.hpack.api.dto.Http2HeaderStruct;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Http2ToHttp1_1 {

	private static Set<String> headersToSkip = new HashSet<>(); 
	static {
		headersToSkip.add(Http2HeaderName.METHOD.getHeaderName());
		headersToSkip.add(Http2HeaderName.PATH.getHeaderName());
		headersToSkip.add(Http2HeaderName.SCHEME.getHeaderName());
	}
	
	public static HttpResponse translateResponse(Http2Response headers) {

        HttpResponseStatus status = new HttpResponseStatus();
        HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
        HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		for(Http2Header header : headers.getHeaders()) {
			if(header.getKnownName() == Http2HeaderName.STATUS) {
				fillStatus(header, status);
			} else if("reason".equals(header.getName())) {
				fillReason(header, status);
			} else if(header.getKnownName() == Http2HeaderName.SCHEME) {
				//do nothing and drop it
			} else {
				Header http1Header = convertHeader(header);
				response.addHeader(http1Header);
			}
		}
		
		if(headers.isEndOfStream() && headers.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_LENGTH) == null) {
			//firefox really needs content length
			response.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		}
		
		if(status.getCode() == null)
			throw new IllegalArgumentException("The header :status is required to send the response");
		
		return response;
	}
	
	private static Header convertHeader(Http2Header header) {
		return new Header(header.getName(), header.getValue());
	}

	private static void fillReason(Http2Header header, HttpResponseStatus status) {
		status.setReason(header.getValue());
	}
	
	private static void fillStatus(Http2Header statusHeader, HttpResponseStatus status) {
        int code = Integer.parseInt(statusHeader.getValue());
        KnownStatusCode knownStatusCode = KnownStatusCode.lookup(code);
        if(knownStatusCode != null) {
        	status.setKnownStatus(knownStatusCode);
        } else {
        	status.setCode(code);
        }		
	}
	
	public static HttpRequest translateRequest(Http2Request headers) {
		
		HttpRequestLine requestLine = new HttpRequestLine();
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		for(Http2Header header : headers.getHeaders()) {
			insertInfo(req, header);
		}

		Http2HeaderStruct headerMap = headers.getHeaderLookupStruct();
		Http2Header method = headerMap.getHeader(Http2HeaderName.METHOD);
		if(method == null)
			throw new IllegalArgumentException(Http2HeaderName.METHOD.name()+"is a required header to translate to http1");
		req.getRequestLine().setMethod(new HttpRequestMethod(method.getValue()));
		
		Http2Header host = headerMap.getHeader(Http2HeaderName.AUTHORITY);
		if(host == null)
			throw new IllegalArgumentException(Http2HeaderName.AUTHORITY.name()+"is a required header to translate to http1");

		
		Http2Header path = headerMap.getHeader(Http2HeaderName.PATH);
		if(path == null)
			throw new IllegalArgumentException(Http2HeaderName.PATH.name()+"is a required header to translate to http1");
		
		HttpUri httpUri = new HttpUri(path.getValue());
		req.getRequestLine().setUri(httpUri );
		
		return req;
	}
	
	private static void insertInfo(HttpRequest req, Http2Header header) {
		if(headersToSkip.contains(header.getName()))
			return;

		if(header.getKnownName() == Http2HeaderName.AUTHORITY) {
			//this keeps header order which is sometimes important
			req.addHeader(new Header(KnownHeaderName.HOST, header.getValue()));
			return;
		}
		
		String name = translateName(header.getName());
		req.addHeader(new Header(name, header.getValue()));		
	}
	
	private static String translateName(String name) {
		char[] charArray = name.toCharArray();
		boolean previousDash = false;
		for(int i = 0; i < charArray.length; i++) {
			if(i == 0)
				charArray[i] = Character.toUpperCase(charArray[i]);
			else if(previousDash)
				charArray[i] = Character.toUpperCase(charArray[i]);
			
			if(charArray[i] == '-')
				previousDash = true;
			else
				previousDash = false;
		}
		return new String(charArray);
	}

	public static HttpData translate(DataFrame data, HttpRequest fromRequest) {
		
		return null;
	}

}
