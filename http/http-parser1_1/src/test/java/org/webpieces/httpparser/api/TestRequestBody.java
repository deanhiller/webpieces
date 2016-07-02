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
import org.webpieces.httpparser.api.dto.HttpRequest;

public class TestRequestBody {

	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testBody() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = parser.marshalToBytes(request);
		DataWrapper wrap1 = dataGen.wrapByteArray(data);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());
		Assert.assertEquals(ParsedStatus.ALL_DATA_PARSED, memento.getStatus());

		HttpPayload msg = memento.getParsedMessages().get(0);
		DataWrapper body = msg.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}

	@Test
	public void testPartialBody() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = parser.marshalToBytes(request);
		
		byte[] first = new byte[data.length - 20];
		byte[] second = new byte[data.length - first.length];
		System.arraycopy(data, 0, first, 0, first.length);
		System.arraycopy(data, first.length, second, 0, second.length);
		DataWrapper wrap1 = dataGen.wrapByteArray(first);
		DataWrapper wrap2 = dataGen.wrapByteArray(second);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(0, memento.getParsedMessages().size());
		Assert.assertEquals(ParsedStatus.NEED_MORE_DATA, memento.getStatus());
		
		memento = parser.parse(memento, wrap2);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());
		Assert.assertEquals(ParsedStatus.ALL_DATA_PARSED, memento.getStatus());

		HttpPayload msg = memento.getParsedMessages().get(0);
		DataWrapper body = msg.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}
	
	@Test
	public void testPartialBodyThenHalfBodyWithNextHttpMsg() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = parser.marshalToBytes(request);
		
		HttpRequest request2 = TestRequestParsing.createPostRequest();
		byte[] payload = parser.marshalToBytes(request2);
		
		byte[] first = new byte[data.length - 20];
		byte[] second = new byte[data.length - first.length + payload.length];
		System.arraycopy(data, 0, first, 0, first.length);
		int lenOfLastPiece = data.length - first.length;
		System.arraycopy(data, first.length, second, 0, lenOfLastPiece);
		System.arraycopy(payload, 0, second, lenOfLastPiece, payload.length);
		DataWrapper wrap1 = dataGen.wrapByteArray(first);
		DataWrapper wrap2 = dataGen.wrapByteArray(second);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(0, memento.getParsedMessages().size());
		Assert.assertEquals(ParsedStatus.NEED_MORE_DATA, memento.getStatus());
		
		memento = parser.parse(memento, wrap2);
		
		Assert.assertEquals(2, memento.getParsedMessages().size());
		Assert.assertEquals(ParsedStatus.ALL_DATA_PARSED, memento.getStatus());

		HttpPayload msg = memento.getParsedMessages().get(0);
		DataWrapper body = msg.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}
	
	private HttpRequest createPostRequestWithBody() {
		byte[] payload = new byte[30];
		for(int i = 0; i < payload.length; i++) {
			payload[i] = (byte) i;
		}
		HttpRequest request = TestRequestParsing.createPostRequest();
		Header header = new Header();
		header.setName(KnownHeaderName.CONTENT_LENGTH);
		header.setValue(""+payload.length);
		
		DataWrapper data = dataGen.wrapByteArray(payload);
		
		request.addHeader(header);
		request.setBody(data);
		
		return request;
	}
}
