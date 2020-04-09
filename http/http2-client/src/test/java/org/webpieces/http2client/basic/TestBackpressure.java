package org.webpieces.http2client.basic;

import java.nio.ByteBuffer;
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
import org.webpieces.http2client.AbstractTest;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockStreamWriter;
import org.webpieces.http2client.util.Requests;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.hpack.api.HpackConfig;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.HpackStatefulParser;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;

public class TestBackpressure extends AbstractTest {

	@Test
	public void testBasicBackpressureChunked() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		StreamHandle handle = httpSocket.openStream();

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		CompletableFuture<StreamWriter> writer = handle.process(request, listener);
		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getFrameAndClear());

		List<ByteBuffer> buffers = create4BuffersWith3Messags();

		DataListener dataListener = mockChannel.getConnectedListener();

		CompletableFuture<Void> fut1 = dataListener.incomingData(mockChannel, buffers.get(0));
		Assert.assertFalse(fut1.isDone()); //not resolved yet since client did not process(only has half the data)
		
		CompletableFuture<StreamWriter> future = new CompletableFuture<StreamWriter>();
		listener.addReturnValueIncomingResponse(future);

		CompletableFuture<Void> fut2 = dataListener.incomingData(mockChannel, buffers.get(1));
		Assert.assertFalse(fut1.isDone()); //not resolved yet since client did not resolve future yet
		Assert.assertFalse(fut2.isDone()); //not resolved yet since client only has part of the data
		
		MockStreamWriter mockWriter = new MockStreamWriter();
		future.complete(mockWriter); //This releases the response msg acking 10 bytes
		
		fut1.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(fut2.isDone());
		
		//feed the rest of first chunk in and feed part of last chunk
		CompletableFuture<Void> firstChunkAck = new CompletableFuture<Void>();
		mockWriter.addProcessResponse(firstChunkAck);	
		CompletableFuture<Void> fut3 = dataListener.incomingData(mockChannel, buffers.get(2));
		
		Assert.assertFalse(fut2.isDone());
		Assert.assertFalse(fut3.isDone());
		
		firstChunkAck.complete(null); //ack the http chunk packet
		
		fut2.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(fut3.isDone());

		CompletableFuture<Void> lastChunkAck = new CompletableFuture<Void>();
		mockWriter.addProcessResponse(lastChunkAck);			
		CompletableFuture<Void> fut4 = dataListener.incomingData(mockChannel, buffers.get(3));
		Assert.assertFalse(fut3.isDone());
		Assert.assertFalse(fut4.isDone());
		
		lastChunkAck.complete(null);
		
		fut3.get(2, TimeUnit.SECONDS);
		fut4.get(2, TimeUnit.SECONDS);
	}
	
	private List<ByteBuffer> create4BuffersWith3Messags() {
		HpackStatefulParser parser = HpackParserFactory.createStatefulParser(new BufferCreationPool(), new HpackConfig("tests"));

		Http2Response response1 = Requests.createResponse(1);
		DataFrame response2 = Requests.createBigData(1, false);
		DataFrame response3 = Requests.createBigData(1, true);
		
		DataWrapper buf1 = parser.marshal(response1);
		DataWrapper buf2 = parser.marshal(response2);
		DataWrapper buf3 = parser.marshal(response3);
		
		//one big wrapper that we can slice up..
		DataWrapper all = DATA_GEN.chainDataWrappers(buf1, buf2, buf3);
		
		byte[] part1 = all.readBytesAt(0, 10);
		byte[] part2 = all.readBytesAt(10, buf1.getReadableSize());
		byte[] part3 = all.readBytesAt(10+buf1.getReadableSize(), buf2.getReadableSize());
		int offset = 10+buf1.getReadableSize()+buf2.getReadableSize();
		int remaining = all.getReadableSize() - offset;
		byte[] part4 = all.readBytesAt(offset, remaining);

		List<ByteBuffer> buffers = new ArrayList<>();
		buffers.add(ByteBuffer.wrap(part1));
		buffers.add(ByteBuffer.wrap(part2));
		buffers.add(ByteBuffer.wrap(part3));
		buffers.add(ByteBuffer.wrap(part4));
		
		return buffers;
	}
}
