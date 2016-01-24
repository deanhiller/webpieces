package com.webpieces.httpparser.api;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;
import com.webpieces.httpparser.api.dto.HttpMethod;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.api.dto.RequestLine;

public class TestRequestParsing {
	
	@Test
	public void testBasic() {
		HttpParser parser = HttpParserFactory.createParser();

		RequestLine requestLine = new RequestLine();
		requestLine.setMethod(HttpMethod.POST);
		requestLine.setUri(new HttpUri("http://myhost.com"));
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(requestLine);
		
		System.out.println("request="+request);

		String result1 = request.toString();
		String result2 = parser.marshalToString(request);
		
		Assert.assertEquals("POST http://myhost.com HTTP/1.1\r\n", result1);
		Assert.assertEquals("POST http://myhost.com HTTP/1.1\r\n", result2);
		
	}

}
