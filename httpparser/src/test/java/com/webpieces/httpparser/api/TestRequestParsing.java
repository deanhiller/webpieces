package com.webpieces.httpparser.api;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpRequestMethod;
import com.webpieces.httpparser.api.dto.HttpUri;

public class TestRequestParsing {
	
	@Test
	public void testBasic() {
		HttpParser parser = HttpParserFactory.createParser();

		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(HttpRequestMethod.POST);
		requestLine.setUri(new HttpUri("http://myhost.com"));
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(requestLine);
		
		String result1 = request.toString();
		String result2 = parser.marshalToString(request);
		
		String msg = "POST http://myhost.com HTTP/1.1\r\n\r\n";
		Assert.assertEquals(msg, result1);
		Assert.assertEquals(msg, result2);
		
//		HttpMessage req = parser.unmarshal(msg.getBytes());
//		Assert.assertEquals(HttpMessageType.REQUEST, req.getMessageType());
//		HttpRequest httpReq = req.getHttpRequest();
//		
//		Assert.assertEquals(request, httpReq);
	}

	@Test
	public void testWithHeaders() {
		HttpParser parser = HttpParserFactory.createParser();

		Header header1 = new Header();
		header1.setName(KnownHeaderName.ACCEPT);
		header1.setValue("CooolValue");
		Header header2 = new Header();
		//let's keep the case even though name is case-insensitive..
		header2.setName("CustomerHEADER");
		header2.setValue("betterValue");
		
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(HttpRequestMethod.POST);
		requestLine.setUri(new HttpUri("http://myhost.com"));
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(requestLine);
		request.addHeader(header1);
		request.addHeader(header2);
		
		String result1 = request.toString();
		String result2 = parser.marshalToString(request);
		
		String msg = "POST http://myhost.com HTTP/1.1\r\n"
				+ "Accept : CooolValue\r\n"
				+ "CustomerHEADER : betterValue\r\n"
				+ "\r\n";
		
		Assert.assertEquals(msg, result1);
		Assert.assertEquals(msg, result2);
		
//		HttpMessage req = parser.unmarshal(msg.getBytes());
//		Assert.assertEquals(HttpMessageType.REQUEST, req.getMessageType());
//		HttpRequest httpReq = req.getHttpRequest();
//		
//		Assert.assertEquals(request, httpReq);
	}
	
}
