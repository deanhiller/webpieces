package org.webpieces.webserver.json;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.After;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.json.app.EchoStreamingClient;
import org.webpieces.webserver.json.app.FakeAuthService;
import org.webpieces.webserver.test.http11.Requests;
import org.webpieces.webserver.test.sockets.AbstractWebpiecesTest;
import org.webpieces.webserver.test.sockets.MockChannel;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestStreamingRaw extends AbstractWebpiecesTest {
	private static final Logger log = LoggerFactory.getLogger(TestStreaming.class);
	private static DataWrapperGenerator gen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private MockAuthService mockAuth = new MockAuthService();
	private MockStreamingClient mockStreamClient = new MockStreamingClient();
	private HttpStatefulParser parser;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("jsonMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(new SimpleMeterRegistry()), new TestOverrides(), true, metaFile);
		webserver.start();
		
		parser = HttpParserFactory.createStatefulParser("testParser", new SimpleMeterRegistry(), new TwoPools(new SimpleMeterRegistry()));
	}

	@After
	public void teardown() {
		//not exactly part of this test but checking for leak of server context into client
		// (only in embedded modes does this occur)
		Assert.assertEquals(0, Context.getContext().size());
	}

	@Test
	public void testAsyncJsonGet() throws InterruptedException, ExecutionException {
		MockChannel channel = new MockChannel();
		XFuture<DataListener> future = mgr.simulateHttpConnect(channel);
		DataListener dataListener = future.get();

		HttpFullRequest fullRequest = Requests.createJsonRequest(KnownHttpMethod.GET, "/json/streaming");
		
		ByteBuffer buffer = parser.marshalToByteBuffer(fullRequest.getRequest());
		DataWrapper data = fullRequest.getData();
		byte[] headers = new byte[buffer.remaining()];
		buffer.get(headers );
		
		data.getReadableSize();
		byte[] part1 = data.readBytesAt(0, 10);
		String str = new String(part1);
		byte[] part2 = data.readBytesAt(10, data.getReadableSize()-10);
		String str2 = new String(part2);

		ByteBuffer firstPacket = ByteBuffer.allocate(headers.length+part1.length);
		firstPacket.put(headers);
		firstPacket.put(part1);
		firstPacket.flip();
		
		XFuture<Boolean> authFuture = new XFuture<Boolean>();
		mockAuth.addValueToReturn(authFuture);
		
		//Feed in request with content-length AND part of the body as well...
		XFuture<Void> fut1 = dataListener.incomingData(channel, firstPacket);

		//Feed in more BEFORE authFuture is complete(this was the bug, ie. race condition)
		ByteBuffer buf2 = ByteBuffer.allocate(part2.length);
		buf2.put(part2);
		buf2.flip();
		XFuture<Void> fut2 = dataListener.incomingData(channel, buf2);
		
		XFuture<StreamWriter> streamWriterFuture = new XFuture<StreamWriter>();
		mockStreamClient.addStreamWriter(streamWriterFuture );
		authFuture.complete(true); //complete it
		
		Assert.assertFalse(fut1.isDone());
		Assert.assertFalse(fut2.isDone());
		MockStreamWriter2 mockStreamWriter = new MockStreamWriter2();
		streamWriterFuture.complete(mockStreamWriter);

		Assert.assertTrue(fut1.isDone());
		Assert.assertTrue(fut2.isDone());
		
		List<StreamMsg> frames = mockStreamWriter.getFrames();
		
		Assert.assertEquals(2, frames.size());
		
		DataFrame f1 = (DataFrame) frames.get(0);
		DataFrame f2 = (DataFrame) frames.get(1);
		
		String s1 = f1.getData().createStringFromUtf8(0, f1.getData().getReadableSize());
		Assert.assertEquals(str, s1);
		String s2 = f2.getData().createStringFromUtf8(0, f2.getData().getReadableSize());
		Assert.assertEquals(str2, s2);
	}
	
	private class TestOverrides implements Module {

		@Override
		public void configure(Binder binder) {
			binder.bind(FakeAuthService.class).toInstance(mockAuth);
			binder.bind(EchoStreamingClient.class).toInstance(mockStreamClient);
			
		}
		
	}
	
}
