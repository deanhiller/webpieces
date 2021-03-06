package org.webpieces.httpparser.api.simulations;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpLastData;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.recording.api.Playback;
import org.webpieces.recording.api.RecordingPlaybackFactory;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestGooglePlayback {

	private HttpParser parser = HttpParserFactory.createParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testGooglePlayback() {
		List<HttpPayload> results = runPlayback("google.com.11015.recording");

		int size = 0;
		for(HttpPayload result : results) {
			if(result.getMessageType() != HttpMessageType.DATA)
				continue;
			
			size += result.getHttpData().getBodyNonNull().getReadableSize();
		}
		Assert.assertEquals(10349, size);
		
		HttpResponse resp = (HttpResponse) results.get(0);
		HttpLastData lastChunk = (HttpLastData) results.get(results.size() - 1);
		
		Assert.assertEquals("chunked", resp.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING).getValue());
		Assert.assertTrue(lastChunk.isEndOfData());
	}

	@Test
	public void testHttpsGooglePlayback() {
		List<HttpPayload> results = runPlayback("https.google.com.recording");

		int size = 0;
		for(HttpPayload result : results) {
			if(result.getMessageType() != HttpMessageType.DATA)
				continue;
			
			size += result.getHttpData().getBodyNonNull().getReadableSize();
		}
		Assert.assertEquals(10396, size);
		
		HttpResponse resp = (HttpResponse) results.get(0);
		HttpLastData lastChunk = (HttpLastData) results.get(results.size()-1);
		
		Assert.assertEquals("chunked", resp.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING).getValue());
		Assert.assertTrue(lastChunk.isEndOfData());		
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
