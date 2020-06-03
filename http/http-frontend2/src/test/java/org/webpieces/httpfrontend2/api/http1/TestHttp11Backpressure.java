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
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2translations.api.Http11ToHttp2;
import org.webpieces.httpfrontend2.api.mock2.MockStreamRef;
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

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;


public class TestHttp11Backpressure extends AbstractHttp1Test {

	@Test
	public void testInitializeThenPostWithChunkingBackpressure() throws InterruptedException, ExecutionException, TimeoutException {
		//send first request to get past protocoal detection(ie. initialize) and send response
		initialize();
		
		List<ByteBuffer> buffers = create4SplitPayloads();
		
		CompletableFuture<Void> ackBytePayload1 = mockChannel.sendToSvr(buffers.get(0));
		Assert.assertTrue(ackBytePayload1.isDone());//not enough data.  parser consumes and acks future for more data(no client ack needed right now)
		
		CompletableFuture<StreamWriter> ackRequest = new CompletableFuture<StreamWriter>();
		mockListener.addMockStreamToReturn(new MockStreamRef(ackRequest));

		CompletableFuture<Void> ackBytePayload2 = mockChannel.sendToSvr(buffers.get(1));
		Assert.assertFalse(ackBytePayload2.isDone());

		ackRequest.complete(mockStreamWriter); //This releases the response msg acking 10 bytes
		Assert.assertTrue(ackBytePayload2.isDone());
		
		//feed the rest of first chunk in and feed part of last chunk
		CompletableFuture<Void> firstChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(firstChunkAck);	
		CompletableFuture<Void> ackBytePayload3 = mockChannel.sendToSvr(buffers.get(2));
		Assert.assertFalse(ackBytePayload3.isDone());
		
		firstChunkAck.complete(null); //ack the http chunk packet
		Assert.assertTrue(ackBytePayload3.isDone());
		
		CompletableFuture<Void> lastChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(lastChunkAck);			
		CompletableFuture<Void> ackBytePayload4 = mockChannel.sendToSvr(buffers.get(3));
		Assert.assertFalse(ackBytePayload4.isDone());
		
		lastChunkAck.complete(null);
		Assert.assertTrue(ackBytePayload4.isDone());
	}
	
	private void initialize() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		CompletableFuture<Void> future2 = mockChannel.sendToSvrAsync(req);
		Assert.assertTrue(future2.isDone()); //The default return was a completed future so no backpressure here
		PassedIn in1 = mockListener.getSingleRequest();
		
		HttpResponse resp1 = Requests.createResponse(1);
		Http2Response headers1 = Http11ToHttp2.responseToHeaders(resp1);
		CompletableFuture<StreamWriter> future = in1.stream.process(headers1);
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
		ackBytePayload1.get(2, TimeUnit.SECONDS);
		
		CompletableFuture<StreamWriter> ackRequest = new CompletableFuture<StreamWriter>();
		
		mockListener.addMockStreamToReturn(new MockStreamRef(ackRequest));
		CompletableFuture<Void> ackBytePayload2 = mockChannel.sendToSvr(buffers.get(1));
		//have to ack TWO...the stream writer AND the first HttpData fed in
		Assert.assertFalse(ackBytePayload2.isDone()); 

		CompletableFuture<Void> firstChunkAck1 = new CompletableFuture<Void>();		
		mockStreamWriter.addProcessResponse(firstChunkAck1);
		ackRequest.complete(mockStreamWriter);
		Assert.assertFalse(ackBytePayload2.isDone());

		firstChunkAck1.complete(null);
		Assert.assertTrue(ackBytePayload2.isDone());
		

		
		
		//feed the rest of first chunk in and feed part of last chunk
		CompletableFuture<Void> firstChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(firstChunkAck);	
		CompletableFuture<Void> ackBytePayload3 = mockChannel.sendToSvr(buffers.get(2));
		Assert.assertFalse(ackBytePayload3.isDone());

		firstChunkAck.complete(null); //ack the http chunk packet
		Assert.assertTrue(ackBytePayload3.isDone());

		CompletableFuture<Void> lastChunkAck = new CompletableFuture<Void>();
		mockStreamWriter.addProcessResponse(lastChunkAck);			
		CompletableFuture<Void> ackBytePayload4 = mockChannel.sendToSvr(buffers.get(3));
		Assert.assertFalse(ackBytePayload4.isDone());
		
		lastChunkAck.complete(null);
		
		ackBytePayload4.get(2, TimeUnit.SECONDS);
	}

	private List<ByteBuffer> create4SplitPayloads() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		req.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		HttpChunk chunk = new HttpChunk();
		String bodyStr = "hi here and there";
		DataWrapper data = DATA_GEN.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		chunk.setBody(data);		
		
		HttpLastChunk lastChunk = new HttpLastChunk();
		
		HttpStatefulParser parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));

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
