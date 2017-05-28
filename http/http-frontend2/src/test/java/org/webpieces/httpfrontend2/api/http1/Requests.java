package org.webpieces.httpfrontend2.api.http1;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.net.URLEncoder;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Requests {

	private static DataWrapperGenerator gen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	public static HttpRequest createRequest(KnownHttpMethod method, String url, boolean isHttps) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(method);
		requestLine.setUri(httpUri);
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		req.addHeader(new Header(KnownHeaderName.HOST, "myhost.com"));

		return req;
	}

	public static HttpRequest createRequest(KnownHttpMethod method, String url) {
		return createRequest(method, url, false);
	}

	public static List<HttpPayload> createPostRequest(String url, String ... argTuples) {
		if(argTuples.length % 2 != 0)
			throw new IllegalArgumentException("argTuples.length must be of even size (key/value)");
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.POST);
		requestLine.setUri(httpUri);
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		
		req.addHeader(new Header(KnownHeaderName.HOST, "myhost.com"));

		String encodedParams = "";
		for(int i = 0; i < argTuples.length; i+=2) {
			String key = URLEncoder.encode(argTuples[i], StandardCharsets.UTF_8);
			String value = URLEncoder.encode(argTuples[i+1], StandardCharsets.UTF_8);
			if(!"".equals(encodedParams))
				encodedParams += "&";
			encodedParams += key+"="+value;
		}
		
		HttpData data = new HttpData();
		data.setEndOfData(true);
		
		byte[] bytes = encodedParams.getBytes(StandardCharsets.UTF_8);
		DataWrapper body = gen.wrapByteArray(bytes);
		data.setBody(body);

		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, ""+body.getReadableSize()));
		req.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		
		List<HttpPayload> payloads = new ArrayList<>();
		payloads.add(req);
		payloads.add(data);
		
		return payloads;
	}

	public static HttpResponse createResponse() {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP_200_OK);

		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		
		HttpResponse resp = new HttpResponse();
		resp.setStatusLine(statusLine);
		
		resp.addHeader(new Header(KnownHeaderName.CONNECTION, "keep"));
		resp.addHeader(new Header(KnownHeaderName.AGE, "hh"));
		return resp;
	}
	
	public static HttpResponse createNobodyResponse() {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP_200_OK);

		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		
		HttpResponse resp = new HttpResponse();
		resp.setStatusLine(statusLine);
		
		resp.addHeader(new Header(KnownHeaderName.CONNECTION, "keep"));
		resp.addHeader(new Header(KnownHeaderName.AGE, "hh"));
		resp.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		return resp;
	}
}
