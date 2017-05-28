package org.webpieces.frontend2.impl.translation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpRequestMethod;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpparser.api.dto.UrlInfo;

import com.webpieces.hpack.api.dto.Http2HeaderStruct;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class Http2Translations {

	private static Set<String> headersToSkip = new HashSet<>(); 
	static {
		headersToSkip.add(Http2HeaderName.METHOD.getHeaderName());
		headersToSkip.add(Http2HeaderName.PATH.getHeaderName());
		headersToSkip.add(Http2HeaderName.SCHEME.getHeaderName());
	}
	
	public static Http2Msg translate(HttpPayload payload, boolean isHttps) {
		if(payload instanceof HttpRequest)
			return requestToHeaders((HttpRequest) payload, isHttps);
		else if(payload instanceof HttpLastChunk)
			return translateChunk((HttpChunk)payload, true);
		else if(payload instanceof HttpChunk)
			return translateChunk((HttpChunk)payload, false);
		else if(payload instanceof HttpData)
			return translateData((HttpData)payload);
		throw new UnsupportedOperationException("not supported yet="+payload);
	}
	

	private static DataFrame translateData(HttpData payload) {
		DataFrame frame = new DataFrame();
		frame.setEndOfStream(payload.isEndOfData());
		frame.setData(payload.getBodyNonNull());
		return frame;
	}

	private static Http2Msg translateChunk(HttpChunk payload, boolean eos) {
		DataFrame frame = new DataFrame();
		frame.setData(payload.getBodyNonNull());
		frame.setEndOfStream(eos);
		return frame;
	}

	public static Http2Response responseToHeaders(HttpResponse response) {
        List<Http2Header> headers = new ArrayList<>();
        headers.add(new Http2Header(Http2HeaderName.STATUS, response.getStatusLine().getStatus().getCode().toString()));

        for(Header header: response.getHeaders()) {
        	if(header.getKnownName() == KnownHeaderName.TRANSFER_ENCODING) {
        		if("chunked".equals(header.getValue())) {
        			continue; //skip as http2 does not allow this header
        		}
        	}
        	
        	headers.add(new Http2Header(header.getName(), header.getValue()));
        }

        Http2Response resp = new Http2Response(headers);

        Header header = response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
        if(header != null) {
        	int len = Integer.parseInt(header.getValue());
        	if(len == 0) {
        		resp.setEndOfStream(true);
        	} else {
        		resp.setEndOfStream(false);
        	}
        } else {
        	resp.setEndOfStream(false);
        }
        
        return resp;
	}

	private static Http2Request requestToHeaders(HttpRequest request, boolean fromSslChannel) {
        HttpRequestLine requestLine = request.getRequestLine();
        List<Header> requestHeaders = request.getHeaders();

        LinkedList<Http2Header> headerList = new LinkedList<>();

        // add special headers
        headerList.add(new Http2Header(":method", requestLine.getMethod().getMethodAsString()));

        UrlInfo urlInfo = requestLine.getUri().getUriBreakdown();
        headerList.add(new Http2Header(":path", urlInfo.getFullPath()));

        // Figure out scheme
        if(urlInfo.getPrefix() != null) {
            headerList.add(new Http2Header(":scheme", urlInfo.getPrefix()));
        } else if(fromSslChannel) {
                headerList.add(new Http2Header(":scheme", "https"));
        } else {
        	headerList.add(new Http2Header(":scheme", "http"));
        }

        // Figure out authority
        
        Header hostHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.HOST);
        if(hostHeader == null)
        	throw new IllegalArgumentException("Host header is required in http1.1");


        // Add regular headers
        for(Header header: requestHeaders) {
        	if(header.getKnownName() == KnownHeaderName.HOST) {
        		//keeps headers in order of http1 headers
                String h = hostHeader.getValue();
                headerList.add(new Http2Header(":authority", h)); 
                continue;
        	}
            headerList.add(new Http2Header(header.getName().toLowerCase(), header.getValue()));
        }

        Http2Request headers = new Http2Request(headerList);
    	headers.setEndOfStream(false);

        return headers;
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

	public static DataFrame translateBody(DataWrapper body) {
		DataFrame data = new DataFrame();
		data.setData(body);
		data.setEndOfStream(true);
		return data;
	}
    
}
