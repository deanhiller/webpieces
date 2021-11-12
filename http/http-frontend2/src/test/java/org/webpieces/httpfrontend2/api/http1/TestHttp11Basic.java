package org.webpieces.httpfrontend2.api.http1;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2translations.api.Http11ToHttp2;
import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpLastData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.streaming.StreamWriter;


public class TestHttp11Basic extends AbstractHttp1Test {
	
	@Test
	public void testConnectionKeepAliveOffAndFrontEndCloses() {
		
	}
	@Test
	public void testConnectionKeepAliveOn() {
		
	}	
	
		
	@Test
	public void testFileUploadWithMultipartFormData() {
		
	}

	@Test
	public void testFileDownloadWithChunking() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2ToHttp11.translateRequest(in1.request);
		Assert.assertEquals(req, req1);
		
		HttpResponse resp = Requests.createResponse();
		resp.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
		Http2Response headers = Http11ToHttp2.responseToHeaders(resp);
		XFuture<StreamWriter> future = in1.stream.process(headers);
		HttpResponse respToClient = (HttpResponse) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, respToClient);
		
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		
		DataFrame dataFrame = new DataFrame();
		dataFrame.setEndOfStream(true);
		String bodyStr = "hi here and there";
		DataWrapper data = DATA_GEN.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		dataFrame.setData(data);
		writer.processPiece(dataFrame);
		
		List<HttpPayload> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(2, frames.size());
		HttpData chunk = (HttpData) frames.get(0);
		DataWrapper body = chunk.getBodyNonNull();
		String result = body.createStringFromUtf8(0, body.getReadableSize());
		Assert.assertEquals(bodyStr, result);

		HttpLastData last = (HttpLastData) frames.get(1);
		Assert.assertEquals(0, last.getBodyNonNull().getReadableSize());
	}
	
	@Test
	public void testPostWithChunking() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		req.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2ToHttp11.translateRequest(in1.request);
		Assert.assertEquals(req, req1);

		HttpChunk chunk = new HttpChunk();
		String bodyStr = "hi here and there";
		DataWrapper data = DATA_GEN.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		chunk.setBody(data);
		mockChannel.sendToSvr(chunk);
		
		DataFrame singleFrame = (DataFrame) mockStreamWriter.getSingleFrame();
		Assert.assertTrue(!singleFrame.isEndOfStream());
		DataWrapper data2 = singleFrame.getData();
		String result = data2.createStringFromUtf8(0, data2.getReadableSize());
		Assert.assertEquals(bodyStr, result);
		
		HttpLastChunk last = new HttpLastChunk();
		mockChannel.sendToSvr(last);
		DataFrame lastEmpty = (DataFrame) mockStreamWriter.getSingleFrame();
		Assert.assertTrue(lastEmpty.isEndOfStream());
		Assert.assertEquals(0, lastEmpty.getData().getReadableSize());
	}
	
	@Test
	public void testUploadWithBody() throws InterruptedException, ExecutionException, TimeoutException {
		String bodyStr = "hi there, how are you";
		DataWrapper dataWrapper = DATA_GEN.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpData body = new HttpData(dataWrapper, true);
		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, ""+dataWrapper.getReadableSize()));

		mockChannel.sendToSvr(req);
		mockChannel.sendToSvr(body);
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2ToHttp11.translateRequest(in1.request);
		Assert.assertEquals(req, req1);

		DataFrame frame = (DataFrame) mockStreamWriter.getSingleFrame();
		DataWrapper data = frame.getData();
		Assert.assertEquals(bodyStr, data.createStringFromUtf8(0, data.getReadableSize()));
		Assert.assertTrue(frame.isEndOfStream());
		
		HttpResponse resp = Requests.createNobodyResponse();
		Http2Response http2Resp = Http11ToHttp2.responseToHeaders(resp);
		XFuture<StreamWriter> fut = in1.stream.process(http2Resp);
		fut.get(2, TimeUnit.SECONDS);
		
		HttpResponse respToClient = (HttpResponse) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, respToClient);
	}

	@Test
	public void testSendTwoRequests() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		XFuture<Void> future = mockChannel.sendToSvrAsync(req2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		Assert.assertFalse(future.isDone());

		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));

		Http2Response headers1 = Http11ToHttp2.responseToHeaders(resp1);
		in1.stream.process(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);
		future.get(2, TimeUnit.SECONDS);

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http11ToHttp2.responseToHeaders(resp2);
		in2.stream.process(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}

	@Test
	public void testSendTwoRequestsStreamFirst() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "20"));

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();

		byte[] buf = new byte[10];
		DataWrapper dataWrapper = DATA_GEN.wrapByteArray(buf);
		HttpData data1 = new HttpData(dataWrapper, false);
		mockChannel.sendToSvr(data1);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		
		
		DataWrapper dataWrapper2 = DATA_GEN.wrapByteArray(buf);
		HttpData data2 = new HttpData(dataWrapper2, true);
		mockChannel.sendToSvr(data2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		
		
		XFuture<Void> fut = mockChannel.sendToSvrAsync(req2);
		Assert.assertFalse(fut.isDone());
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers1 = Http11ToHttp2.responseToHeaders(resp1);
		in1.stream.process(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);

		fut.get(2, TimeUnit.SECONDS);
		
		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http11ToHttp2.responseToHeaders(resp2);
		in2.stream.process(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}
	
	@Test
	public void testSendTwoRequestsStreamSecond() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();

		req2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "20"));
		XFuture<Void> fut = mockChannel.sendToSvrAsync(req2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		Assert.assertFalse(fut.isDone());

		byte[] buf = new byte[10];
		DataWrapper dataWrapper = DATA_GEN.wrapByteArray(buf);
		HttpData data1 = new HttpData(dataWrapper, false);
		XFuture<Void> fut2 = mockChannel.sendToSvrAsync(data1);
		Assert.assertFalse(fut.isDone());
		Assert.assertFalse(fut2.isDone());

		DataWrapper dataWrapper2 = DATA_GEN.wrapByteArray(buf);
		HttpData data2 = new HttpData(dataWrapper2, true);
		XFuture<Void> fut3 = mockChannel.sendToSvrAsync(data2);

		Assert.assertFalse(fut.isDone());
		Assert.assertFalse(fut2.isDone());
		Assert.assertFalse(fut3.isDone());
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers1 = Http11ToHttp2.responseToHeaders(resp1);
		in1.stream.process(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);

		fut.get(2, TimeUnit.SECONDS);
		fut2.get(2, TimeUnit.SECONDS);
		fut3.get(2, TimeUnit.SECONDS);

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http11ToHttp2.responseToHeaders(resp2);
		in2.stream.process(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}
	
	@Test
	public void testSendTwoRequestsStreamFirstResponse() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		XFuture<Void> fut1 = mockChannel.sendToSvrAsync(req2);
		Assert.assertFalse(fut1.isDone());
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());

		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "10"));
		Http2Response headers1 = Http11ToHttp2.responseToHeaders(resp1);
		XFuture<StreamWriter> future = in1.stream.process(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		
		Assert.assertFalse(fut1.isDone());
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		byte[] buf = new byte[10];
		DataWrapper dataWrapper = DATA_GEN.wrapByteArray(buf);
		HttpData data1 = new HttpData(dataWrapper, true);
		DataFrame data = (DataFrame) Http11ToHttp2.translateData(data1);
		writer.processPiece(data);
		
		fut1.get(2, TimeUnit.SECONDS);

		HttpData d = (HttpData) mockChannel.getFrameAndClear();
		Assert.assertEquals(10, d.getBody().getReadableSize());

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http11ToHttp2.responseToHeaders(resp2);
		in2.stream.process(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}
}
