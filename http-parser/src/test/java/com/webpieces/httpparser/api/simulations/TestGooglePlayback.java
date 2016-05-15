package com.webpieces.httpparser.api.simulations;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.recording.api.Playback;
import org.webpieces.recording.api.RecordingPlaybackFactory;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpLastChunk;
import com.webpieces.httpparser.api.dto.HttpPayload;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class TestGooglePlayback {

	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testGooglePlayback() {
		List<HttpPayload> results = runPlayback("google.com.11015.recording");

		Assert.assertEquals(3, results.size());
		
		HttpResponse resp = (HttpResponse) results.get(0);
		HttpChunk chunk = (HttpChunk) results.get(1);
		HttpLastChunk lastChunk = (HttpLastChunk) results.get(2);
		
		Assert.assertEquals("chunked", resp.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING).getValue());
		Assert.assertEquals(10349, chunk.getBody().getReadableSize());
		Assert.assertTrue(lastChunk.isLastChunk());
	}

	@Test
	public void testHttpsGooglePlayback() {
		List<HttpPayload> results = runPlayback("https.google.com.recording");

		Assert.assertEquals(3, results.size());
		
		HttpResponse resp = (HttpResponse) results.get(0);
		HttpChunk chunk = (HttpChunk) results.get(1);
		HttpLastChunk lastChunk = (HttpLastChunk) results.get(2);
		
		Assert.assertEquals("chunked", resp.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING).getValue());
		Assert.assertEquals(10396, chunk.getBody().getReadableSize());
		Assert.assertTrue(lastChunk.isLastChunk());		
	}
	
	private List<HttpPayload> runPlayback(String name) {
		Memento mem = parser.prepareToParse();
		
		int counter = 0;
		ClassLoader cl = getClass().getClassLoader();
		InputStream in = cl.getResourceAsStream(name);
		//This loads relative to this test class package(while the above does not).
		//InputStream in = getClass().getResourceAsStream(name);
		
		Playback playback = RecordingPlaybackFactory.createPlayback(in, 1);
		List<HttpPayload> results = new ArrayList<>();
		
		while(true) {
			counter++;
			if(counter > 1000)
				throw new IllegalArgumentException("Is your simulation really this long...1000+ buffers?");
			ByteBuffer buffer = playback.getNextPacket();
			if(buffer == null)
				return results;

			DataWrapper data = dataGen.wrapByteBuffer(buffer);
			mem = parser.parse(mem, data);
			List<HttpPayload> parsedMessages = mem.getParsedMessages();
			results.addAll(parsedMessages);
		}
	}
}
