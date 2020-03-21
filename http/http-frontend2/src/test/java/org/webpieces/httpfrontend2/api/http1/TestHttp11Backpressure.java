package org.webpieces.httpfrontend2.api.http1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2translations.api.Http1_1ToHttp2;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;


public class TestHttp11Backpressure extends AbstractHttp1Test {

	@Test
	public void testInitializeThenPostWithChunkingBackpressure() throws InterruptedException, ExecutionException, TimeoutException {
		//send first request to get past protocoal detection(ie. initialize) and send response
		initialize();
		
		List<ByteBuffer> buffers = create4SplitPayloads();
		
		CompletableFuture<Void> ackBytePayload1 = mockChannel.sendToSvr(buffers.get(0));
		Assert.assertFalse(ackBytePayload1.isDone());
		
		CompletableFuture<StreamWriter> ackRequest = new CompletableFuture<StreamWriter>();
		mockListener.addMockStreamToReturn(ackRequest);
		CompletableFuture<Void> ackBytePayload2 = mockChannel.sendToSvr(buffers.get(1));
		Assert.assertFalse(ackBytePayload1.isDone());
		Assert.assertFalse(ackBytePayload2.isDone());

		ackRequest.complete(mockStreamWriter); //This releases the response msg acking 10 bytes
		ackBytePayload1.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(ackBytePayload2.isDone());
		
		//feed the rest of first chunk in and feed part of last chunk
		CompletableFuture<Void> firstChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(firstChunkAck);	
		CompletableFuture<Void> ackBytePayload3 = mockChannel.sendToSvr(buffers.get(2));

		Assert.assertFalse(ackBytePayload2.isDone());
		Assert.assertFalse(ackBytePayload3.isDone());
		
		firstChunkAck.complete(null); //ack the http chunk packet
		
		ackBytePayload2.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(ackBytePayload3.isDone());

		CompletableFuture<Void> lastChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(lastChunkAck);			
		CompletableFuture<Void> ackBytePayload4 = mockChannel.sendToSvr(buffers.get(3));
		Assert.assertFalse(ackBytePayload3.isDone());
		Assert.assertFalse(ackBytePayload4.isDone());
		
		lastChunkAck.complete(null);
		
		ackBytePayload3.get(2, TimeUnit.SECONDS);
		ackBytePayload4.get(2, TimeUnit.SECONDS);
	}
	
	private void initialize() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		HttpResponse resp1 = Requests.createResponse(1);
		Http2Response headers1 = Http1_1ToHttp2.responseToHeaders(resp1);
		CompletableFuture<StreamWriter> future = in1.stream.sendResponse(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		
		//server should add content-length 0 for firefox
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Assert.assertEquals(resp1, payload);
		future.get(2, TimeUnit.SECONDS);
	}

	@Test
	public void testPostWithChunkingBackpressure() throws InterruptedException, ExecutionException, TimeoutException {
		List<ByteBuffer> buffers = create4SplitPayloads();
		
		CompletableFuture<Void> ackBytePayload1 = mockChannel.sendToSvr(buffers.get(0));
		//server side, backpressure ONLY starts working after first request headers fully processed as it has to determine
		//the protocol
		ackBytePayload1.get(2, TimeUnit.SECONDS);
		
		CompletableFuture<StreamWriter> ackRequest = new CompletableFuture<StreamWriter>();
		mockListener.addMockStreamToReturn(ackRequest);
		CompletableFuture<Void> ackBytePayload2 = mockChannel.sendToSvr(buffers.get(1));
		ackBytePayload2.get(2, TimeUnit.SECONDS); //not resolved yet since client only has part of the data
		
		//feed the rest of first chunk in and feed part of last chunk
		CompletableFuture<Void> firstChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(firstChunkAck);	
		CompletableFuture<Void> ackBytePayload3 = mockChannel.sendToSvr(buffers.get(2));

		ackRequest.complete(mockStreamWriter);

		Assert.assertFalse(ackBytePayload3.isDone());
		
		firstChunkAck.complete(null); //ack the http chunk packet
		
		ackBytePayload2.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(ackBytePayload3.isDone());

		CompletableFuture<Void> lastChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(lastChunkAck);			
		CompletableFuture<Void> ackBytePayload4 = mockChannel.sendToSvr(buffers.get(3));
		Assert.assertFalse(ackBytePayload3.isDone());
		Assert.assertFalse(ackBytePayload4.isDone());
		
		lastChunkAck.complete(null);
		
		ackBytePayload3.get(2, TimeUnit.SECONDS);
		ackBytePayload4.get(2, TimeUnit.SECONDS);
	}

	private List<ByteBuffer> create4SplitPayloads() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		req.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		HttpChunk chunk = new HttpChunk();
		String bodyStr = "hi here and there";
		DataWrapper data = dataGen.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		chunk.setBody(data);		
		
		HttpLastChunk lastChunk = new HttpLastChunk();
		
		HttpStatefulParser parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new BufferCreationPool());

		ByteBuffer buf1 = parser.marshalToByteBuffer(req);
		ByteBuffer buf2 = parser.marshalToByteBuffer(chunk);
		ByteBuffer buf3 = parser.marshalToByteBuffer(lastChunk);

		byte[] part1 = new byte[10];
		byte[] part2 = new byte[buf1.remaining()];
		buf1.get(part1);
		int toWrite = buf1.remaining();
		buf1.get(part2, 0, toWrite);
		buf2.get(part2, toWrite, part2.length-toWrite);

		byte[] part3 = new byte[buf2.remaining()+2];
		int toWrite2 = buf2.remaining();
		buf2.get(part3, 0, toWrite2);
		buf3.get(part3, toWrite2, 2);
		
		byte[] part4 = new byte[buf3.remaining()];
		buf3.get(part4);
		
		List<ByteBuffer> buffers = new ArrayList<>();
		buffers.add(ByteBuffer.wrap(part1));
		buffers.add(ByteBuffer.wrap(part2));
		buffers.add(ByteBuffer.wrap(part3));
		buffers.add(ByteBuffer.wrap(part4));
		
		return buffers;
	}
	
}
