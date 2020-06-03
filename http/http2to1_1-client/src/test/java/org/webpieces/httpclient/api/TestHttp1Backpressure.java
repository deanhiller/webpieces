package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpclient.api.mocks.MockChannel;
import org.webpieces.httpclient.api.mocks.MockChannelMgr;
import org.webpieces.httpclient.api.mocks.MockResponseListener;
import org.webpieces.httpclient.api.mocks.MockStreamWriter;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestHttp1Backpressure {

	private MockChannelMgr mockChannelMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private Http2Client httpClient;
	private Http2Socket socket;

	@Before
	public void setup() throws InterruptedException, ExecutionException, TimeoutException {
		BufferPool pool = new TwoPools("pl", new SimpleMeterRegistry());
		httpClient = Http2to11ClientFactory.createHttpClient("myClient3", mockChannelMgr, new SimpleMeterRegistry(), pool);
		
		mockChannelMgr.addTCPChannelToReturn(mockChannel);
		socket = httpClient.createHttpSocket();

		mockChannel.setConnectFuture(CompletableFuture.completedFuture(null));
		CompletableFuture<Void> future = socket.connect(new InetSocketAddress(8080));
		
		future.get(2, TimeUnit.SECONDS);
	}

	@Test
	public void testBasicBackpressure() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		RequestStreamHandle handle = socket.openStream();

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		StreamRef streamRef = handle.process(request, listener);
		CompletableFuture<StreamWriter> writer = streamRef.getWriter();

		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		Http2Response response1 = Requests.createResponse(1, 250);
		HttpData response2 = Requests.createHttpData(250);
		List<ByteBuffer> buffers = create3BuffersWithTwoMessags(response1, response2);

		DataListener dataListener = mockChannel.getConnectedListener();

		CompletableFuture<StreamWriter> requestFuture = new CompletableFuture<StreamWriter>();
		listener.addProcessResponse(requestFuture);

		CompletableFuture<Void> fut1 = dataListener.incomingData(mockChannel, buffers.get(0));
		Assert.assertTrue(fut1.isDone()); //resolved since data is cached in parser (client doesn't receive this data)
		
		//This next one is confusing BUT in http1.1 parsing terms, data is data for content length so this results
		//in a full HttpData packet actually...
		CompletableFuture<Void> fut2 = dataListener.incomingData(mockChannel, buffers.get(1));
		Assert.assertFalse(fut2.isDone()); //not resolved yet since client only has part of the data
		
		MockStreamWriter mockWriter = new MockStreamWriter();
		CompletableFuture<Void> streamWriterFuture = new CompletableFuture<Void>();
		mockWriter.addProcessResponse(streamWriterFuture);
		requestFuture.complete(mockWriter); //This releases BOTH packets above to be processed!!!(not just the one)
		Assert.assertFalse(fut2.isDone());
		
		streamWriterFuture.complete(null);
		fut2.get(2, TimeUnit.SECONDS); //NOW it's resolved

		//feed the last buffer in
		CompletableFuture<Void> future3 = new CompletableFuture<Void>();
		mockWriter.addProcessResponse(future3);		
		CompletableFuture<Void> fut3 = dataListener.incomingData(mockChannel, buffers.get(2));		
		Assert.assertFalse(fut3.isDone());
		
		future3.complete(null);
		fut3.get(2, TimeUnit.SECONDS);
	}

	private List<ByteBuffer> create3BuffersWithTwoMessags(Http2Response response1, HttpData response2) {
		HttpStatefulParser parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));

		HttpResponse resp1 = Http2ToHttp11.translateResponse(response1);
		
		ByteBuffer buf1 = parser.marshalToByteBuffer(resp1);
		ByteBuffer buf2 = parser.marshalToByteBuffer(response2);

		byte[] part1 = new byte[10];
		byte[] part2 = new byte[buf1.remaining()];
		buf1.get(part1);
		int toWrite = buf1.remaining();
		buf1.get(part2, 0, toWrite);
		buf2.get(part2, toWrite, part2.length-toWrite);

		byte[] part3 = new byte[buf2.remaining()];
		buf2.get(part3);
		
		List<ByteBuffer> buffers = new ArrayList<>();
		buffers.add(ByteBuffer.wrap(part1));
		buffers.add(ByteBuffer.wrap(part2));
		buffers.add(ByteBuffer.wrap(part3));
		
		return buffers;
	}
	
	@Test
	public void testBasicBackpressureChunked() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		RequestStreamHandle handle = socket.openStream();

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		StreamRef streamRef = handle.process(request, listener);
		CompletableFuture<StreamWriter> writer = streamRef.getWriter();

		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		Http2Response response1 = Requests.createChunkedResponse(1);
		HttpChunk response2 = Requests.createHttpChunk(250);
		HttpLastChunk lastChunk = new HttpLastChunk();
		List<ByteBuffer> buffers = create4BuffersWith3Messags(response1, response2, lastChunk);

		DataListener dataListener = mockChannel.getConnectedListener();

		CompletableFuture<Void> fut1 = dataListener.incomingData(mockChannel, buffers.get(0));
		Assert.assertTrue(fut1.isDone()); //resolved since it never made it to the client. 
		                               //this keeps bytes in-memory that is less than bufPool.getSuggestedSize. ie. < 5k 
		
		CompletableFuture<StreamWriter> requestFuture = new CompletableFuture<StreamWriter>();
		listener.addProcessResponse(requestFuture);
		
		CompletableFuture<Void> fut2 = dataListener.incomingData(mockChannel, buffers.get(1));
		Assert.assertFalse(fut2.isDone()); //not resolved yet since client only has part of the data
		
		CompletableFuture<Void> streamWriterFuture = new CompletableFuture<Void>();
		MockStreamWriter mockWriter = new MockStreamWriter();
		mockWriter.addProcessResponse(streamWriterFuture);
		requestFuture.complete(mockWriter); //This releases the response BUT 'some' data was with response so can't ack yet
		Assert.assertFalse(fut2.isDone()); //not resolved yet since requestFuture AND streamWriterFutuer BOTH need to be resolved
		                                   //This is BECAUSE 68 bytes resulted in TWO messages and both futures can ack the 68 bytes.
		
		streamWriterFuture.complete(null);
		fut2.get(2, TimeUnit.SECONDS); //NOW it's resolved
		
		//feed the rest of first chunk in and feed part of last chunk
		CompletableFuture<Void> firstChunkAck = new CompletableFuture<Void>();
		mockWriter.addProcessResponse(firstChunkAck);	
		CompletableFuture<Void> fut3 = dataListener.incomingData(mockChannel, buffers.get(2));
		
		Assert.assertFalse(fut3.isDone());
		
		firstChunkAck.complete(null); //ack the http chunk packet
		
		fut3.get(2, TimeUnit.SECONDS);

		CompletableFuture<Void> lastChunkAck = new CompletableFuture<Void>();
		mockWriter.addProcessResponse(lastChunkAck);			
		CompletableFuture<Void> fut4 = dataListener.incomingData(mockChannel, buffers.get(3));
		Assert.assertFalse(fut4.isDone());
		
		lastChunkAck.complete(null);
		
		fut4.get(2, TimeUnit.SECONDS);
	}
	
	private List<ByteBuffer> create4BuffersWith3Messags(Http2Response response1, HttpChunk response2, HttpLastChunk lastChunk) {
		HttpStatefulParser parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));

		HttpResponse resp1 = Http2ToHttp11.translateResponse(response1);
		
		ByteBuffer buf1 = parser.marshalToByteBuffer(resp1);
		ByteBuffer buf2 = parser.marshalToByteBuffer(response2);
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
