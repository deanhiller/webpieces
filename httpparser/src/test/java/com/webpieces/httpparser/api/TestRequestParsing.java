package com.webpieces.httpparser.api;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpRequestMethod;
import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.impl.ConvertAscii;

public class TestRequestParsing {
	
	private HttpParser parser = HttpParserFactory.createParser();
	private DataWrapperGenerator dataGen = HttpParserFactory.createDataWrapperGenerator();
	
	@Test
	public void testBasic() {
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
	public void testAsciiConverter() {
		HttpRequest request = createRequest();
		byte[] payload = parser.marshalToBytes(request);
		ConvertAscii converter = new ConvertAscii();
		String readableForm = converter.convertToReadableForm(payload);
		Assert.assertEquals(
				"POST\\s http://myhost.com\\s HTTP/1.1\\r\\n\r\n"
				+ "Accept\\s :\\s CooolValue\\r\\n\r\n"
				+ "CustomerHEADER\\s :\\s betterValue\\r\\n\r\n"
				+ "\\r\\n\r\n", 
				readableForm);
	}
	
	@Test
	public void testWithHeaders() {
		HttpRequest request = createRequest();
		
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

	@Test
	public void testPartialHttpMessage() {
		HttpRequest request = createRequest();
		byte[] payload = parser.marshalToBytes(request);
		
		byte[] firstPart = new byte[10];
		byte[] secondPart = new byte[payload.length-firstPart.length];
		//let's split the payload up into two pieces
		System.arraycopy(payload, 0, firstPart, 0, firstPart.length);
		System.arraycopy(payload, firstPart.length, secondPart, 0, secondPart.length);
		
		DataWrapper data1 = dataGen.wrapByteArray(firstPart);
		DataWrapper data2 = dataGen.wrapByteArray(secondPart);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, data1);
		
		Assert.assertEquals(ParsedStatus.NEED_MORE_DATA, memento.getStatus());
		Assert.assertEquals(0, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data2);
		
		Assert.assertEquals(ParsedStatus.ALL_DATA_PARSED, memento.getStatus());
		Assert.assertEquals(1, memento.getParsedMessages().size());
		
		HttpMessage httpMessage = memento.getParsedMessages().get(0);
		Assert.assertEquals(request,  httpMessage);
	}
	
	@Test
	public void test2AndHalfHttpMessages() {
	}
	
	/**
	 * Send in 1/2 first http message and then send in 
	 * next 1/2 AND 1/2 of second message TOGETHER in 2nd
	 * payload of bytes to make sure it is handled correctly
	 * and then finally last 1/2
	 */
	@Test
	public void testHalfThenTwoHalvesNext() {
		
	}
	
	@Test
	public void testExactlyOneHttpMessage() {
		
	}
	
	private HttpRequest createRequest() {
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
		return request;
	}
	
}
