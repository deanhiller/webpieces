package org.webpieces.httpfrontend2.api.http2;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class TestSMaxConcurrentSetting extends AbstractHttp2Test {
	
	@Test
	public void testSend2ndPushHeadersOnlyOnCompletionOfFirst() throws InterruptedException, ExecutionException, TimeoutException {
		WriterHolder sent = sendTwoRequests();

		DataFrame data1 = Http2Requests.createData1(sent.getResp1().getStreamId(), true);
		//ending this promise stream starts the next
		sent.getWriter1().send(data1);
		
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(2, frames.size());
		DataFrame dataRecv1 = (DataFrame) frames.get(0);
		Assert.assertEquals(sent.getResp1().getStreamId(), dataRecv1.getStreamId());
		Assert.assertEquals(sent.getResp2(), frames.get(1));
		
		StreamWriter writer2 = sent.getFuture2().get(2, TimeUnit.SECONDS);
		
		DataFrame data2 = Http2Requests.createData1(sent.getResp2().getStreamId(), true);
		writer2.send(data2);
		
		DataFrame dataRecv2 = (DataFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(sent.getResp2().getStreamId(), dataRecv2.getStreamId());

	}
	
	@Test
	public void testSend2ndPushHeadersOnlyOnAfterSettingsFrameMaxConcurrentBigger() throws InterruptedException, ExecutionException, TimeoutException {
		WriterHolder sent = sendTwoRequests();

		//client increases max concurrent
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(2L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		
		Assert.assertEquals(2, frames.size());
		Assert.assertEquals(sent.getResp2(), frames.get(0));
		SettingsFrame dataRecv1 = (SettingsFrame) frames.get(1);
		Assert.assertTrue(dataRecv1.isAck());
		
		StreamWriter writer2 = sent.getFuture2().get(2, TimeUnit.SECONDS);
		
		DataFrame data2 = Http2Requests.createData1(sent.getResp2().getStreamId(), true);
		writer2.send(data2);
		
		DataFrame dataRecv2 = (DataFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(sent.getResp2().getStreamId(), dataRecv2.getStreamId());
	}
	
	private WriterHolder sendTwoRequests() throws InterruptedException, ExecutionException, TimeoutException {
		
		PassedIn in1 = sendRequestToServer(1, true);
		PassedIn in2 = sendRequestToServer(3, true);
		
		Assert.assertTrue(in1.stream != in2.stream);
		
		Http2Push push1 = Http2Requests.createPush(in1.request.getStreamId());
		CompletableFuture<StreamWriter> future1 = in1.stream.sendPush(push1);
		Http2Msg push1Recv = mockChannel.getFrameAndClear();
		Assert.assertEquals(push1, push1Recv);
		StreamWriter writer1 = future1.get(2, TimeUnit.SECONDS);

		Http2Push push2 = Http2Requests.createPush(in2.request.getStreamId());
		CompletableFuture<StreamWriter> future2 = in2.stream.sendPush(push2);
		Http2Msg push2Recv = mockChannel.getFrameAndClear();
		Assert.assertEquals(push2, push2Recv);
		Assert.assertEquals(push1, push1Recv);
		StreamWriter writer2 = future2.get(2, TimeUnit.SECONDS);
		
		//send the two responses following the pushes
		Http2Headers resp1 = Http2Requests.createResponse(push1.getPromisedStreamId(), false);
		CompletableFuture<StreamWriter> fut1 = writer1.send(resp1);
		writer1 = fut1.get(2, TimeUnit.SECONDS);
		
		Http2Headers resp2 = Http2Requests.createResponse(push2.getPromisedStreamId(), false);
		CompletableFuture<StreamWriter> fut2 = writer2.send(resp2);
		Assert.assertTrue(!fut2.isDone());
		
		Http2Headers clientRecvResp = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, clientRecvResp);
		
		return new WriterHolder(writer1, fut2, resp1, resp2);
	}

}
