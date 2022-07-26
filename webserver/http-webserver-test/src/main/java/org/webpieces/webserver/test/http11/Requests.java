package org.webpieces.webserver.test.http11;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.net.URLEncoder;


public class Requests {

	private static DataWrapperGenerator gen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	public static final String JSON = "{ `query`: `cats and dogs`, `meta`: { `numResults`: 4 }, `testValidation`:`notBlank1` }".replace("`", "\"");
	
	public static HttpFullRequest createRequest(KnownHttpMethod method, String url) {
		return createRequest(method, url, null);
	}
	
	public static HttpRequest createBaseRequest(KnownHttpMethod method, String url) {
		return createBaseRequest(method, url, null);
	}

	public static HttpFullRequest createRequest(KnownHttpMethod method, String url, Integer port) {
		HttpRequest req = createBaseRequest(method, url, port);
		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}
	
	public static HttpRequest createBaseRequest(KnownHttpMethod method, String url, Integer port) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(method);
		requestLine.setUri(httpUri);
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		if(port == null)
			req.addHeader(new Header(KnownHeaderName.HOST, "myhost.com"));
		else
			req.addHeader(new Header(KnownHeaderName.HOST, "myhost.com:"+port));
		
		return req;
	}

	public static HttpFullRequest createCorsRequestCookie(String fromDomain, String url, List<String> accessHeaders, KnownHttpMethod method, String cookieValue) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(method);
		requestLine.setUri(httpUri);

		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		req.addHeader(new Header(KnownHeaderName.HOST, "api.domain.com"));
		req.addHeader(new Header(KnownHeaderName.ORIGIN, fromDomain));
		for(String name : accessHeaders) {
			req.addHeader(new Header(name, name));
		}
		req.addHeader(new Header(KnownHeaderName.COOKIE, cookieValue));

		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}

	public static HttpFullRequest createCorsRequest(String fromDomain, String url, List<String> accessHeaders, KnownHttpMethod method) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(method);
		requestLine.setUri(httpUri);

		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		req.addHeader(new Header(KnownHeaderName.HOST, "api.domain.com"));
		req.addHeader(new Header(KnownHeaderName.ORIGIN, fromDomain));
		for(String name : accessHeaders) {
			req.addHeader(new Header(name, name));
		}

		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}

	public static HttpFullRequest createNotCorsRequest(String url, List<String> accessHeaders, KnownHttpMethod method) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(method);
		requestLine.setUri(httpUri);

		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		String domain = "api.domain.com";
		req.addHeader(new Header(KnownHeaderName.HOST, domain));
		req.addHeader(new Header(KnownHeaderName.ORIGIN, "https://"+domain));
		for(String name : accessHeaders) {
			req.addHeader(new Header(name, name));
		}

		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}

	public static HttpFullRequest createOptionsPreflightRequest(String fromDomain, String url, String accessHeaders, String method) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.OPTIONS);
		requestLine.setUri(httpUri);

		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		req.addHeader(new Header(KnownHeaderName.HOST, "api.domain.com"));
		req.addHeader(new Header(KnownHeaderName.ORIGIN, fromDomain));
		req.addHeader(new Header(KnownHeaderName.ACCESS_CONTROL_REQUEST_METHOD, method));
		req.addHeader(new Header(KnownHeaderName.ACCESS_CONTROL_REQUEST_HEADERS, accessHeaders));

		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}

	public static HttpFullRequest createGetRequest(String domain, String url) {
		HttpUri httpUri = new HttpUri(url);
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(httpUri);
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);

		req.addHeader(new Header(KnownHeaderName.HOST, domain));

		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}

	public static HttpFullRequest createPostRequest(String url, String ... argTuples) {
		try {
			return createPostRequestImpl(url, argTuples);
		} catch (UnsupportedEncodingException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	private static HttpFullRequest createPostRequestImpl(String url, String ... argTuples) throws UnsupportedEncodingException {
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
		
		byte[] bytes = encodedParams.getBytes(StandardCharsets.UTF_8);
		DataWrapper body = gen.wrapByteArray(bytes);
		
		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, ""+body.getReadableSize()));
		req.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		
		return new HttpFullRequest(req, body);
	}

	public static HttpFullRequest createJsonRequest(KnownHttpMethod method, String url, String json) {
		HttpRequest request = createBaseRequest(method, url);
		
		DataWrapper body = gen.wrapByteArray(json.getBytes());

		request.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		
		return new HttpFullRequest(request, body);
	}
	
	public static HttpFullRequest createJsonRequest(KnownHttpMethod method, String url) {
		HttpRequest request = createBaseRequest(method, url);
		
		DataWrapper body = gen.wrapByteArray(JSON.getBytes());

		request.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		
		return new HttpFullRequest(request, body);
	}

	public static HttpFullRequest createBadJsonRequest(KnownHttpMethod method, String url) {
		HttpRequest request = createBaseRequest(method, url);
		String json = "{ `query `cats and dogs`, `meta`: { `numResults`: 4 }, `testValidation`:`notBlank` }".replace("`", "\"");
		DataWrapper body = gen.wrapByteArray(json.getBytes());

		request.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, body.getReadableSize()+""));
		
		return new HttpFullRequest(request, body);
	}
	
}
