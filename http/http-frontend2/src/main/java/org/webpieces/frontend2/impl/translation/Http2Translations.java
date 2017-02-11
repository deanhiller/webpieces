package org.webpieces.frontend2.impl.translation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.UrlInfo;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Http2Translations {

    public static Http2Headers responseToHeaders(HttpResponse response) {
        List<Http2Header> headers = new ArrayList<>();
        headers.add(new Http2Header(":status", response.getStatusLine().getStatus().getCode().toString()));
        for(Header header: response.getHeaders()) {
            headers.add(new Http2Header(header.getName(), header.getValue()));
        }
        
    	Http2Headers req = new Http2Headers(headers);
    	req.setEndOfStream(true);
        return req;
    }
    
    LinkedList<Http2Header> requestToHeaders(HttpRequest request, boolean fromSslChannel) {
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
        String h = null;
        for(Header header: requestHeaders) {
            if(header.getKnownName().equals(KnownHeaderName.HOST)) {
                h = header.getValue();
                break;
            }
        }
        if(h != null) {
            headerList.add(new Http2Header(":authority", h));
        }

        // Add regular headers
        for(Header header: requestHeaders) {
            headerList.add(new Http2Header(header.getName().toLowerCase(), header.getValue()));
        }

        return headerList;
    }

	public static Http2Msg translate(HttpPayload payload) {
		// TODO Auto-generated method stub
		return null;
	}

	public static HttpResponse translateResponse(Http2Headers headers) {
		// TODO Auto-generated method stub
		return null;
	}

	public static HttpPayload translate(PartialStream data) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
