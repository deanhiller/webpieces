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
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2translations.api.Http2ToHttp1_1;
import org.webpieces.httpclient.api.mocks.MockChannel;
import org.webpieces.httpclient.api.mocks.MockChannelMgr;
import org.webpieces.httpclient.api.mocks.MockResponseListener;
import org.webpieces.httpclient.api.mocks.MockStreamWriter;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

public class TestWriteReads {

	private MockChannelMgr mockChannelMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private Http2Client httpClient;
	private Http2Socket socket;

	@Before
	public void setup() {
		BufferPool pool = new BufferCreationPool();
		httpClient = Http2to1_1ClientFactory.createHttpClient(mockChannelMgr, pool);
		
		mockChannelMgr.addTCPChannelToReturn(mockChannel);
		socket = httpClient.createHttpSocket("clientSocket1");

		mockChannel.setConnectFuture(CompletableFuture.completedFuture(null));
		CompletableFuture<Http2Socket> future = socket.connect(new InetSocketAddress(8080));
		
		Assert.assertTrue(future.isDone());
	}

	@Test
	public void testBasicReadWrite() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		StreamHandle handle = socket.openStream();

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		CompletableFuture<StreamWriter> writer = handle.process(request, listener);
		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		MockResponseListener listener2 = new MockResponseListener();
		request.getHeaderLookupStruct().getHeader("serverid").setValue("2");
		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		CompletableFuture<StreamWriter> writer2 = handle.process(request, listener2);
		Assert.assertTrue(writer2.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		Http2Response response1 = Requests.createResponse(1, 0);
		listener.addProcessResponse(CompletableFuture.completedFuture(null));
		CompletableFuture<Void> fut1 = mockChannel.writeResponse(response1);
		fut1.get(2, TimeUnit.SECONDS); //throws if exception occurred and ensures future resolved
		
		Http2Response msg = listener.getIncomingMsg();
		Assert.assertEquals(response1, msg);
		
		Http2Response response2 = Requests.createResponse(2, 0);
		listener2.addProcessResponse(CompletableFuture.completedFuture(null));
		CompletableFuture<Void> fut2 = mockChannel.writeResponse(response2);
		fut2.get(2, TimeUnit.SECONDS); //throws if exception occurred and ensures future resolved
		Http2Response msg2 = listener2.getIncomingMsg();
		Assert.assertEquals(response2, msg2);
	}

	//@Test
	public void testBasicBackpressure() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		StreamHandle handle = socket.openStream();

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		CompletableFuture<StreamWriter> writer = handle.process(request, listener);
		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		Http2Response response1 = Requests.createResponse(1, 250);
		HttpData response2 = Requests.createHttpChunk(250);
		List<ByteBuffer> buffers = create3BuffersWithTwoMessags(response1, response2);

		DataListener dataListener = mockChannel.getConnectedListener();

		CompletableFuture<StreamWriter> future = new CompletableFuture<StreamWriter>();
		listener.addProcessResponse(future);

		CompletableFuture<Void> fut1 = dataListener.incomingData(mockChannel, buffers.get(0));
		Assert.assertFalse(fut1.isDone()); //not resolved yet since client did not process(only has half the data)
		
		CompletableFuture<Void> fut2 = dataListener.incomingData(mockChannel, buffers.get(1));
		Assert.assertFalse(fut1.isDone()); //not resolved yet since client did not resolve future yet
		Assert.assertFalse(fut2.isDone()); //not resolved yet since client only has part of the data
		
		MockStreamWriter mockWriter = new MockStreamWriter();
		CompletableFuture<StreamWriter> future2 = new CompletableFuture<StreamWriter>();
		mockWriter.addProcessResponse(future2);
		future.complete(mockWriter);
		
		fut1.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(fut2.isDone());
		
		future2.complete(mockWriter);
		
		fut2.get(2, TimeUnit.SECONDS);
	}

	private List<ByteBuffer> create3BuffersWithTwoMessags(Http2Response response1, HttpData response2) {
		HttpStatefulParser parser = HttpParserFactory.createStatefulParser(new BufferCreationPool());

		HttpResponse resp1 = Http2ToHttp1_1.translateResponse(response1);
		
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
}
