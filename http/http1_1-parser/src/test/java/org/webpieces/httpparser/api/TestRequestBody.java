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
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestRequestBody {

	private HttpParser parser = HttpParserFactory.createParser("a", new SimpleMeterRegistry(), new BufferCreationPool());
	private MarshalState state = parser.prepareToMarshal();

	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private byte[] unwrap(ByteBuffer buffer1) {
		byte[] data = new byte[buffer1.remaining()];
		buffer1.get(data);
		return data;
	}
	
	private byte[] unwrap(HttpDummyRequest request) {
		ByteBuffer buffer1 = parser.marshalToByteBuffer(state, request.getRequest());
		ByteBuffer buffer2 = parser.marshalToByteBuffer(state, request.getHttpData());
		
		int size = buffer1.remaining();
		
		byte[] data = new byte[size+buffer2.remaining()];
		buffer1.get(data, 0, size);
		buffer2.get(data, size, buffer2.remaining());
		return data;
	}
	
	@Test
	public void testBody() {
		HttpDummyRequest request = createPostRequestWithBody();
		byte[] expected = request.getHttpData().getBody().createByteArray();
		
		byte[] data = unwrap(request);
		DataWrapper wrap1 = dataGen.wrapByteArray(data);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(2, memento.getParsedMessages().size());

		HttpPayload msg = memento.getParsedMessages().get(0);
		Assert.assertEquals(request.getRequest(), msg);
		HttpData data1 = (HttpData) memento.getParsedMessages().get(1);
		DataWrapper body = data1.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}

	@Test
	public void testPartialBody() {
		HttpDummyRequest request = createPostRequestWithBody();
		byte[] expected = request.getHttpData().getBody().createByteArray();

		byte[] data = unwrap(request);
		
		byte[] first = new byte[data.length - 20];
		byte[] second = new byte[data.length - first.length];
		System.arraycopy(data, 0, first, 0, first.length);
		System.arraycopy(data, first.length, second, 0, second.length);
		DataWrapper wrap1 = dataGen.wrapByteArray(first);
		DataWrapper wrap2 = dataGen.wrapByteArray(second);
		
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrap1);
		
		Assert.assertEquals(ParsingState.BODY, memento.getUnParsedState().getCurrentlyParsing());
		Assert.assertEquals(0, memento.getUnParsedState().getCurrentUnparsedSize());
		
		HttpRequest httpRequest = memento.getParsedMessages().get(0).getHttpRequest();
		Assert.assertEquals(request.getRequest(), httpRequest);
		HttpData data1 = memento.getParsedMessages().get(1).getHttpData();
		
		Assert.assertEquals(2, memento.getParsedMessages().size());
		
		memento = parser.parse(memento, wrap2);
		
		Assert.assertEquals(1, memento.getParsedMessages().size());

		HttpData data2 = memento.getParsedMessages().get(0).getHttpData();
		DataWrapper body = dataGen.chainDataWrappers(data1.getBody(), data2.getBody());
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
	}
	
	@Test
	public void testPartialBodyThenHalfBodyWithNextHttpMsg() {
		HttpDummyRequest request = createPostRequestWithBody();
		byte[] expected = request.getHttpData().getBody().createByteArray();

		byte[] data = unwrap(request);
		
		HttpRequest request2 = TestRequestParsing.createPostRequest();
		byte[] payload = unwrap(parser.marshalToByteBuffer(state, request2));
		
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
		
		Assert.assertEquals(2, memento.getParsedMessages().size());
		HttpPayload msg = memento.getParsedMessages().get(0);
		Assert.assertEquals(request.getRequest(), msg);
		HttpData data1 = (HttpData) memento.getParsedMessages().get(1);
		
		memento = parser.parse(memento, wrap2);
		
		Assert.assertEquals(2, memento.getParsedMessages().size());

		HttpData data2 = (HttpData) memento.getParsedMessages().get(0);

		DataWrapper body = dataGen.chainDataWrappers(data1.getBody(), data2.getBody());
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
		
		HttpPayload msg2 = memento.getParsedMessages().get(1);
		Assert.assertEquals(request2, msg2);
	}
	
	@Test
	public void testPartialHeaders() {
		HttpDummyRequest request = createPostRequestWithBody();
		byte[] expected = request.getHttpData().getBody().createByteArray();

		byte[] data = unwrap(request);
		
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
		
		Assert.assertEquals(2, memento.getParsedMessages().size());

		HttpPayload msg = memento.getParsedMessages().get(0);
		Assert.assertEquals(request.getRequest(), msg);
		HttpData data1 = (HttpData) memento.getParsedMessages().get(1);
		DataWrapper body = data1.getBody();
		byte[] bodyBytesActual = body.createByteArray();
		Assert.assertArrayEquals(expected, bodyBytesActual);
		
	}
	
	private HttpDummyRequest createPostRequestWithBody() {
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
		HttpData httpData = new HttpData(data, true);
		
		return new HttpDummyRequest(request, httpData);
	}
}
