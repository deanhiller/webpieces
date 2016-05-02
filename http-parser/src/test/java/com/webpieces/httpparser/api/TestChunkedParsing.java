package com.webpieces.httpparser.api;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class TestChunkedParsing {
	
	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testHex() {
		for(int i = 0; i < 50; i++) {
			String string = Integer.toHexString(i);
			int val = Integer.parseInt(string, 16);
			Assert.assertEquals(i, val);
		}
	}
	
	@Test
	public void testBasic() {
		String chunkedData = "4\r\nWiki\r\n5\r\npedia\r\nE\r\n in\r\n\r\nchunks.\r\n0\r\n\r\n";
		
		HttpResponse resp = TestResponseParsing.createOkResponse();
		resp.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
		
		byte[] bytes = parser.marshalToBytes(resp);
		byte[] chunked = chunkedData.getBytes();
		
		byte[] all = new byte[bytes.length+chunked.length];
		System.arraycopy(bytes, 0, all, 0, bytes.length);
		System.arraycopy(chunked, 0, all, bytes.length, chunked.length);
		
		DataWrapper wrapper = dataGen.wrapByteArray(all);
		Memento memento = parser.prepareToParse();
		memento = parser.parse(memento, wrapper);
		
		List<HttpMessage> msgs = memento.getParsedMessages();
		Assert.assertEquals(5, msgs.size());
		
		HttpMessage msg = msgs.get(0).getHttpResponse();
		Assert.assertEquals(resp, msg);
		
		HttpChunk chunk1 = msgs.get(1).getHttpChunk();
		String first = chunk1.getBody().createStringFrom(0, chunk1.getBody().getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals("Wiki", first);
		
		HttpChunk chunk3 = msgs.get(3).getHttpChunk();
		String third = chunk3.getBody().createStringFrom(0, chunk3.getBody().getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals(" in\r\n\r\nchunks.", third);
	}

	@Test
	public void testResponseAfterChunked() {
		
	}
	
	@Test
	public void testSplitChunkExtensions() {
		
	}
	
	@Test
	public void testSplitChunkBody() {
		
	}
	
	@Test
	public void testMultipleExtensions() {
		
	}
	
	//http://stackoverflow.com/questions/5590791/http-chunked-encoding-need-an-example-of-trailer-mentioned-in-spec
	@Test
	public void testLastChunkContainsTrailingHeaders() {
		
	}
}
