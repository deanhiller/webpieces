package org.webpieces.httpfrontend2.api.http1;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;


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

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2Translations.translateRequest(in1.request);
		Assert.assertEquals(req, req1);
		
		HttpResponse resp = Requests.createResponse();
		resp.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
		Http2Response headers = Http2Translations.responseToHeaders(resp);
		CompletableFuture<StreamWriter> future = in1.stream.sendResponse(headers);
		HttpResponse respToClient = (HttpResponse) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, respToClient);
		
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		
		DataFrame dataFrame = new DataFrame();
		dataFrame.setEndOfStream(true);
		String bodyStr = "hi here and there";
		DataWrapper data = dataGen.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		dataFrame.setData(data);
		writer.processPiece(dataFrame);
		
		List<HttpPayload> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(2, frames.size());
		HttpChunk chunk = (HttpChunk) frames.get(0);
		DataWrapper body = chunk.getBodyNonNull();
		String result = body.createStringFromUtf8(0, body.getReadableSize());
		Assert.assertEquals(bodyStr, result);

		HttpLastChunk last = (HttpLastChunk) frames.get(1);
		Assert.assertEquals(0, last.getBodyNonNull().getReadableSize());
	}
	
	@Test
	public void testPostWithChunking() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		req.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2Translations.translateRequest(in1.request);
		Assert.assertEquals(req, req1);

		HttpChunk chunk = new HttpChunk();
		String bodyStr = "hi here and there";
		DataWrapper data = dataGen.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		chunk.setBody(data);
		mockChannel.write(chunk);
		
		DataFrame singleFrame = (DataFrame) mockStreamWriter.getSingleFrame();
		Assert.assertTrue(!singleFrame.isEndOfStream());
		DataWrapper data2 = singleFrame.getData();
		String result = data2.createStringFromUtf8(0, data2.getReadableSize());
		Assert.assertEquals(bodyStr, result);
		
		HttpLastChunk last = new HttpLastChunk();
		mockChannel.write(last);
		DataFrame lastEmpty = (DataFrame) mockStreamWriter.getSingleFrame();
		Assert.assertTrue(lastEmpty.isEndOfStream());
		Assert.assertEquals(0, lastEmpty.getData().getReadableSize());
	}
	
	@Test
	public void testUploadWithBody() throws InterruptedException, ExecutionException, TimeoutException {
		String bodyStr = "hi there, how are you";
		DataWrapper dataWrapper = dataGen.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpData body = new HttpData(dataWrapper, true);
		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, ""+dataWrapper.getReadableSize()));

		mockChannel.write(req);
		mockChannel.write(body);
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2Translations.translateRequest(in1.request);
		Assert.assertEquals(req, req1);

		DataFrame frame = (DataFrame) mockStreamWriter.getSingleFrame();
		DataWrapper data = frame.getData();
		Assert.assertEquals(bodyStr, data.createStringFromUtf8(0, data.getReadableSize()));
		Assert.assertTrue(frame.isEndOfStream());
		
		HttpResponse resp = Requests.createNobodyResponse();
		Http2Response http2Resp = Http2Translations.responseToHeaders(resp);
		CompletableFuture<StreamWriter> fut = in1.stream.sendResponse(http2Resp);
		fut.get(2, TimeUnit.SECONDS);
		
		HttpResponse respToClient = (HttpResponse) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, respToClient);
	}

	@Test
	public void testSendTwoRequests() throws InterruptedException, ExecutionException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		mockChannel.write(req2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());

		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));

		Http2Response headers1 = Http2Translations.responseToHeaders(resp1);
		in1.stream.sendResponse(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http2Translations.responseToHeaders(resp2);
		in2.stream.sendResponse(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}

	@Test
	public void testSendTwoRequestsStreamFirst() throws InterruptedException, ExecutionException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "20"));

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();

		byte[] buf = new byte[10];
		DataWrapper dataWrapper = dataGen.wrapByteArray(buf);
		HttpData data1 = new HttpData(dataWrapper, false);
		mockChannel.write(data1);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		
		
		DataWrapper dataWrapper2 = dataGen.wrapByteArray(buf);
		HttpData data2 = new HttpData(dataWrapper2, true);
		mockChannel.write(data2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		
		
		mockChannel.write(req2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers1 = Http2Translations.responseToHeaders(resp1);
		in1.stream.sendResponse(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http2Translations.responseToHeaders(resp2);
		in2.stream.sendResponse(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}
	
	@Test
	public void testSendTwoRequestsStreamSecond() throws InterruptedException, ExecutionException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();

		req2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "20"));
		mockChannel.write(req2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		byte[] buf = new byte[10];
		DataWrapper dataWrapper = dataGen.wrapByteArray(buf);
		HttpData data1 = new HttpData(dataWrapper, false);
		mockChannel.write(data1);
		
		DataWrapper dataWrapper2 = dataGen.wrapByteArray(buf);
		HttpData data2 = new HttpData(dataWrapper2, true);
		mockChannel.write(data2);

		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers1 = Http2Translations.responseToHeaders(resp1);
		in1.stream.sendResponse(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http2Translations.responseToHeaders(resp2);
		in2.stream.sendResponse(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}
	
	@Test
	public void testSendTwoRequestsStreamFirstResponse() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		mockChannel.write(req2);
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());

		HttpResponse resp1 = Requests.createResponse(1);
		resp1.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "10"));
		Http2Response headers1 = Http2Translations.responseToHeaders(resp1);
		CompletableFuture<StreamWriter> future = in1.stream.sendResponse(headers1);
		HttpPayload payload = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, payload);
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());		

		byte[] buf = new byte[10];
		DataWrapper dataWrapper = dataGen.wrapByteArray(buf);
		HttpData data1 = new HttpData(dataWrapper, true);
		DataFrame data = (DataFrame) Http2Translations.translateData(data1);
		writer.processPiece(data);
		
		HttpData d = (HttpData) mockChannel.getFrameAndClear();
		Assert.assertEquals(10, d.getBody().getReadableSize());

		PassedIn in2 = mockListener.getSingleRequest();
		HttpResponse resp2 = Requests.createResponse(2);
		resp2.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, "0"));
		Http2Response headers2 = Http2Translations.responseToHeaders(resp2);
		in2.stream.sendResponse(headers2);
		
		HttpPayload payload2 = mockChannel.getFrameAndClear();		
		Assert.assertEquals(resp2, payload2);
	}
}
