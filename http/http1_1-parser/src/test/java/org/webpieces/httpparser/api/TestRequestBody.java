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

public class TestRequestBody {

	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private byte[] unwrap(ByteBuffer buffer) {
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		return data;
	}
	
	@Test
	public void testBody() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = unwrap(parser.marshalToByteBuffer(request));
		DataWrapper wrap1 = dataGen.wrapByteArray(data);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());

		HttpPayload msg = memento.getParsedMessages().get(0);
		DataWrapper body = msg.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}

	@Test
	public void testPartialBody() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = unwrap(parser.marshalToByteBuffer(request));
		
		byte[] first = new byte[data.length - 20];
		byte[] second = new byte[data.length - first.length];
		System.arraycopy(data, 0, first, 0, first.length);
		System.arraycopy(data, first.length, second, 0, second.length);
		DataWrapper wrap1 = dataGen.wrapByteArray(first);
		DataWrapper wrap2 = dataGen.wrapByteArray(second);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(ParsingState.BODY, memento.getUnParsedState().getCurrentlyParsing());
		Assert.assertEquals(10, memento.getUnParsedState().getCurrentUnparsedSize());
		
		HttpPayload payload = memento.getHalfParsedMessage();
		HttpRequest httpRequest = payload.getHttpRequest();
		Header header = httpRequest.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
		Assert.assertEquals("30", header.getValue());
		
		Assert.assertEquals(0, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, wrap2);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());

		HttpPayload msg = memento.getParsedMessages().get(0);
		DataWrapper body = msg.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}
	
	@Test
	public void testPartialBodyThenHalfBodyWithNextHttpMsg() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = unwrap(parser.marshalToByteBuffer(request));
		
		HttpRequest request2 = TestRequestParsing.createPostRequest();
		byte[] payload = unwrap(parser.marshalToByteBuffer(request2));
		
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
		
		memento = parser.parse(memento, wrap2);
		
		Assert.assertEquals(2, memento.getParsedMessages().size());

		HttpPayload msg = memento.getParsedMessages().get(0);
		DataWrapper body = msg.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}
	
	@Test
	public void testPartialHeaders() {
		HttpRequest request = createPostRequestWithBody();
		byte[] expected = request.getBody().createByteArray();
		
		byte[] data = unwrap(parser.marshalToByteBuffer(request));
		
		byte[] first = new byte[20];
		byte[] second = new byte[10];
		byte[] third = new byte[data.length - first.length - second.length];
		System.arraycopy(data, 0, first, 0, first.length);
		System.arraycopy(data, first.length, second, 0, second.length);
		System.arraycopy(data, first.length+second.length, third, 0, third.length);
		DataWrapper wrap1 = dataGen.wrapByteArray(first);
		DataWrapper wrap2 = dataGen.wrapByteArray(second);
		DataWrapper wrap3 = dataGen.wrapByteArray(third);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);

		Assert.assertEquals(ParsingState.HEADERS, memento.getUnParsedState().getCurrentlyParsing());
		Assert.assertEquals(20, memento.getUnParsedState().getCurrentUnparsedSize());
		
		memento = parser.parse(memento, wrap2);

		Assert.assertEquals(ParsingState.HEADERS, memento.getUnParsedState().getCurrentlyParsing());
		Assert.assertEquals(30, memento.getUnParsedState().getCurrentUnparsedSize());
		
		Assert.assertEquals(0, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, wrap3);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());

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
