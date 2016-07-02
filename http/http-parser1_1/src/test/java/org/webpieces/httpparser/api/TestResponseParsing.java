package org.webpieces.httpparser.api;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.ParsedStatus;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpparser.impl.ConvertAscii;

public class TestResponseParsing {
	
	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testBasic() {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP200);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		String result1 = response.toString();
		String result2 = parser.marshalToString(response);
		
		String msg = "HTTP/1.1 200 OK\r\n\r\n";
		Assert.assertEquals(msg, result1);
		Assert.assertEquals(msg, result2);
	}

	@Test
	public void testAsciiConverter() {
		HttpResponse response = createOkResponse();
		byte[] payload = parser.marshalToBytes(response);
		ConvertAscii converter = new ConvertAscii();
		String readableForm = converter.convertToReadableForm(payload);
		Assert.assertEquals(
				"HTTP/1.1\\s 200\\s OK\\r\\n\r\n"
				+ "Accept:\\s CooolValue\\r\\n\r\n"
				+ "CustomerHEADER:\\s betterValue\\r\\n\r\n"
				+ "\\r\\n\r\n", 
				readableForm);
	}
	
	@Test
	public void testWithHeadersAndBody() {
		HttpResponse response = createOkResponse();
		
		String result1 = response.toString();
		String result2 = parser.marshalToString(response);
		
		String msg = "HTTP/1.1 200 OK\r\n"
				+ "Accept: CooolValue\r\n"
				+ "CustomerHEADER: betterValue\r\n"
				+ "\r\n";
		
		Assert.assertEquals(msg, result1);
		Assert.assertEquals(msg, result2);
	}
	
	@Test
	public void testPartialHttpMessage() {
		HttpResponse response = createOkResponse();
		byte[] payload = parser.marshalToBytes(response);
		
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
		
		HttpPayload httpMessage = memento.getParsedMessages().get(0);
		
		Assert.assertEquals(response,  httpMessage);
	}
	
	@Test
	public void test2AndHalfHttpMessages() {
		HttpResponse response = createOkResponse();
		byte[] payload = parser.marshalToBytes(response);
		
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
		
		Assert.assertEquals(ParsedStatus.MSG_PARSED_AND_LEFTOVER_DATA, memento.getStatus());
		Assert.assertEquals(2, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data2);
		
		Assert.assertEquals(ParsedStatus.ALL_DATA_PARSED, memento.getStatus());
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
		HttpResponse request = createOkResponse();
		byte[] payload = parser.marshalToBytes(request);
		
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
		
		Assert.assertEquals(ParsedStatus.NEED_MORE_DATA, memento.getStatus());
		Assert.assertEquals(0, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data2);
		
		Assert.assertEquals(ParsedStatus.MSG_PARSED_AND_LEFTOVER_DATA, memento.getStatus());
		Assert.assertEquals(1, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, data3);
		
		Assert.assertEquals(ParsedStatus.ALL_DATA_PARSED, memento.getStatus());
		Assert.assertEquals(1, memento.getParsedMessages().size());
	}
	
	static HttpResponse createOkResponse() {
		Header header1 = new Header();
		header1.setName(KnownHeaderName.ACCEPT);
		header1.setValue("CooolValue");
		Header header2 = new Header();
		//let's keep the case even though name is case-insensitive..
		header2.setName("CustomerHEADER");
		header2.setValue("betterValue");
		
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP200);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		
		HttpResponse resp = new HttpResponse();
		resp.setStatusLine(statusLine);
		resp.addHeader(header1);
		resp.addHeader(header2);
		return resp;
	}

}
