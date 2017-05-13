package org.webpieces.frontend2.impl.translation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
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
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

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
		throw new UnsupportedOperationException("not supported yet");
	}
	
    private static Http2Headers requestToHeaders(HttpRequest request, boolean fromSslChannel) {
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

    	Http2Headers headers = new Http2Headers(headerList);
    	headers.setEndOfStream(false);

        return headers;
    }



	public static HttpPayload translate(PartialStream data) {
		throw new UnsupportedOperationException("not done yet");
	}

	public static HttpResponse translateResponse(Http2Headers headers) {

        HttpResponseStatus status = new HttpResponseStatus();
        HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
        HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		for(Http2Header header : headers.getHeaders()) {
			if(header.getKnownName() == Http2HeaderName.STATUS) {
				fillStatus(header, status);
			} else {
				Header http1Header = convertHeader(header);
				response.addHeader(http1Header);
			}
		}
		
		if(status.getCode() == null)
			throw new IllegalArgumentException("The header :status is required to send the response");
		
		return response;
	}

	private static Header convertHeader(Http2Header header) {
		return new Header(header.getName(), header.getValue());
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

	public static HttpRequest translateRequest(Http2Headers headers) {
		
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
