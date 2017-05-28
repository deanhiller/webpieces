package org.webpieces.httpparser.api;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.impl.ConvertAscii;

public class TestRequestParsing {
	
	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	private MarshalState state = parser.prepareToMarshal();

	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private byte[] unwrap(ByteBuffer buffer) {
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		return data;
	}
	
	@Test
	public void testBasic() {
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.POST);
		requestLine.setUri(new HttpUri("http://myhost.com"));
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(requestLine);
		
		String result1 = request.toString();
		String result2 = parser.marshalToString(request);
		
		String msg = "POST http://myhost.com HTTP/1.1\r\n\r\n";
		Assert.assertEquals(msg, result1);
		Assert.assertEquals(msg, result2);
	}

	@Test
	public void testAsciiConverter() {
		HttpRequest request = createPostRequest();
		byte[] payload = unwrap(parser.marshalToByteBuffer(state, request));
		ConvertAscii converter = new ConvertAscii();
		String readableForm = converter.convertToReadableForm(payload);
		
		String expected = "POST\\s http://myhost.com\\s HTTP/1.1\\r\\n\r\n"
				+ "Accept:\\s CooolValue\\r\\n\r\n"
				+ "CustomerHEADER:\\s betterValue\\r\\n\r\n"
				+ "\\r\\n\r\n";
		Assert.assertEquals(expected, readableForm);
	}
	
	@Test
	public void testWithHeadersAndBody() {
		HttpRequest request = createPostRequest();
		
		String result1 = request.toString();
		String result2 = parser.marshalToString(request);
		
		String msg = "POST http://myhost.com HTTP/1.1\r\n"
				+ "Accept: CooolValue\r\n"
				+ "CustomerHEADER: betterValue\r\n"
				+ "\r\n";
		
		Assert.assertEquals(msg, result1);
		Assert.assertEquals(msg, result2);
	}
	
	@Test
	public void testPartialHttpMessage() {
		HttpRequest request = createPostRequest();
		byte[] payload = unwrap(parser.marshalToByteBuffer(state, request));
		
		byte[] firstPart = new byte[10];
		byte[] secondPart = new byte[payload.length-firstPart.length];
		//let's split the payload up into two pieces
		System.arraycopy(payload, 0, firstPart, 0, firstPart.length);
		System.arraycopy(payload, firstPart.length, secondPart, 0, secondPart.length);
		
		DataWrapper data1 = dataGen.wrapByteArray(firstPart);
		DataWrapper data2 = dataGen.wrapByteArray(secondPart);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, data1);
		
		Assert.assertEquals(0, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data2);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());
		
		HttpPayload httpMessage = memento.getParsedMessages().get(0);
		HttpRequest req = (HttpRequest) httpMessage;
		
		Assert.assertEquals(request.getRequestLine(), req.getRequestLine());
		Assert.assertEquals(request.getHeaders(),  req.getHeaders());
		
		Assert.assertEquals(request,  httpMessage);
	}
	
	@Test
	public void test2AndHalfHttpMessages() {
		HttpRequest request = createPostRequest();
		byte[] payload = unwrap(parser.marshalToByteBuffer(state, request));
		
		byte[] first = new byte[2*payload.length + 20];
		byte[] second = new byte[payload.length - 20];
		System.arraycopy(payload, 0, first, 0, payload.length);
		System.arraycopy(payload, 0, first, payload.length, payload.length);
		System.arraycopy(payload, 0, first, 2*payload.length, 20);
		System.arraycopy(payload, 20, second, 0, second.length);
		
		DataWrapper data1 = dataGen.wrapByteArray(first);
		DataWrapper data2 = dataGen.wrapByteArray(second);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, data1);
		
		Assert.assertEquals(2, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data2);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());
	}
	
	/**
	 * Send in 1/2 first http message and then send in 
	 * next 1/2 AND 1/2 of second message TOGETHER in 2nd
	 * payload of bytes to make sure it is handled correctly
	 * and then finally last 1/2
	 */
	@Test
	public void testHalfThenTwoHalvesNext() {
		HttpRequest request = createPostRequest();
		byte[] payload = unwrap(parser.marshalToByteBuffer(state, request));
		
		byte[] first = new byte[20];
		byte[] second = new byte[payload.length];
		byte[] third = new byte[payload.length - first.length];
		System.arraycopy(payload, 0, first, 0, first.length);
		System.arraycopy(payload, first.length, second, 0, payload.length-first.length);
		System.arraycopy(payload, 0, second, payload.length-first.length, first.length);
		System.arraycopy(payload, first.length, third, 0, third.length);
		
		DataWrapper data1 = dataGen.wrapByteArray(first);
		DataWrapper data2 = dataGen.wrapByteArray(second);
		DataWrapper data3 = dataGen.wrapByteArray(third);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, data1);
		
		Assert.assertEquals(0, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data2);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data3);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());		
	}
	
	static HttpRequest createPostRequest() {
		Header header1 = new Header();
		header1.setName(KnownHeaderName.ACCEPT);
		header1.setValue("CooolValue");
		Header header2 = new Header();
		//let's keep the case even though name is case-insensitive..
		header2.setName("CustomerHEADER");
		header2.setValue("betterValue");
		
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.POST);
		requestLine.setUri(new HttpUri("http://myhost.com"));
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(requestLine);
		request.addHeader(header1);
		request.addHeader(header2);
		return request;
	}
	

}
