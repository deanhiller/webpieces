package org.webpieces.httpparser.api;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;


public class TestReceiveHttp2 {
	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testReceivePreface() {
		//All the 1's must be ignored and in leftover data 
		String preface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n1111";
		byte[] bytes = preface.getBytes(StandardCharsets.UTF_8);
		DataWrapper moreData = dataGen.wrapByteArray(bytes);
		
		Memento state = parser.prepareToParse();
		state = parser.parse(state, moreData);
		
		Assert.assertEquals(1, state.getParsedMessages().size());
		Assert.assertEquals(4, state.getLeftOverData().getReadableSize());
		
		HttpPayload httpPayload = state.getParsedMessages().get(0);
		Assert.assertEquals(HttpMessageType.HTTP2_MARKER_MSG, httpPayload.getMessageType());
	}
	
}
