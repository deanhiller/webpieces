package org.webpieces.http2translations.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.UrlInfo;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Http11ToHttp2 {
	
	public static Http2Msg translate(HttpPayload payload, boolean isHttps) {
		if(payload instanceof HttpRequest)
			return requestToHeaders((HttpRequest) payload, isHttps);
		else if(payload instanceof HttpLastChunk)
			return translateChunk((HttpChunk)payload, true);
		else if(payload instanceof HttpChunk)
			return translateChunk((HttpChunk)payload, false);
		else if(payload instanceof HttpData)
			return translateData((HttpData)payload);
		throw new UnsupportedOperationException("not supported yet="+payload.getClass());
	}
	

	public static DataFrame translateData(HttpData payload) {
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
        } else if(response.isHasChunkedTransferHeader()) {
        	resp.setEndOfStream(false);
        } else 
        	resp.setEndOfStream(true);
        
        return resp;
	}

	private static Http2Request requestToHeaders(HttpRequest request, boolean fromSslChannel) {
        HttpRequestLine requestLine = request.getRequestLine();
        List<Header> requestHeaders = request.getHeaders();

        LinkedList<Http2Header> headerList = new LinkedList<>();

        // add special headers
        headerList.add(new Http2Header(":method", requestLine.getMethod().getMethodAsString()));

        UrlInfo urlInfo = requestLine.getUri().getUriBreakdown();
        headerList.add(new Http2Header(":path", requestLine.getUri().getUri()));

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
        
        Header contentLen = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);

        if(request.isHasChunkedTransferHeader()) {
        	headers.setEndOfStream(false);        	
        } else if(contentLenGreaterThanZero(contentLen)) {
        	headers.setEndOfStream(false);
        } else
        	headers.setEndOfStream(true);

        return headers;
    }

	private static boolean contentLenGreaterThanZero(Header contentLen) {
		if(contentLen == null)
			return false;
		int len = Integer.parseInt(contentLen.getValue());
		if(len > 0)
			return true;
		return false;
	}

	public static DataFrame translateBody(DataWrapper body) {
		DataFrame data = new DataFrame();
		data.setData(body);
		data.setEndOfStream(true);
		return data;
	}
    
}
