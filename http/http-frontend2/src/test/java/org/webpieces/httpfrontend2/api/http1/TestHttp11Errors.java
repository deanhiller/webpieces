package org.webpieces.httpfrontend2.api.http1;

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
import org.webpieces.httpfrontend2.api.mock2.MockStreamRef;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.streaming.StreamWriter;

public class TestHttp11Errors extends AbstractHttp1Test {

	@Test
	public void testHttp1UploadInterleavesWithHttpRequest() {
		
	}
	
	@Test
	public void testFarEndClosed() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		XFuture<Void> fut = mockChannel.sendToSvrAsync(req2);
		Assert.assertFalse(fut.isDone());
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		mockChannel.simulateClose();

		//nothing to cancel because no requests were sent in
	}
	
	@Test
	public void testRemoteClientClosesSocket() {
		XFuture<StreamWriter> fut = XFuture.completedFuture(null);
		MockStreamRef mockStreamRef = new MockStreamRef(fut);
		mockListener.addMockStreamToReturn(mockStreamRef);
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		req.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2ToHttp11.translateRequest(in1.request);
		Assert.assertEquals(req, req1);

		Assert.assertFalse(mockStreamRef.isCancelled());

		mockChannel.simulateClose();
		
		Assert.assertTrue(mockStreamRef.isCancelled());
	}
	
	@Test
	public void testCloseBeforeFirstRequestCompletes() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		XFuture<StreamWriter> futA = XFuture.completedFuture(null);
		MockStreamRef mockStreamRefA = new MockStreamRef(futA);
		mockListener.addMockStreamToReturn(mockStreamRefA);
		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		

		XFuture<StreamWriter> futB = XFuture.completedFuture(null);
		MockStreamRef mockStreamRefB = new MockStreamRef(futB);
		mockListener.addMockStreamToReturn(mockStreamRefB);
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

		
		Assert.assertFalse(mockStreamRefA.isCancelled());
		Assert.assertFalse(mockStreamRefB.isCancelled());
		
		mockChannel.simulateClose();
		
		Assert.assertTrue(mockStreamRefA.isCancelled()); //this request is done, nothing to cancel
		Assert.assertFalse(mockStreamRefB.isCancelled());		
	}
	
	@Test
	public void testCloseAfter2ndRequest() throws InterruptedException, ExecutionException, TimeoutException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");

		XFuture<StreamWriter> futA = XFuture.completedFuture(null);
		MockStreamRef mockStreamRefA = new MockStreamRef(futA);
		mockListener.addMockStreamToReturn(mockStreamRefA);
		mockChannel.sendToSvr(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		

		XFuture<StreamWriter> futB = XFuture.completedFuture(null);
		MockStreamRef mockStreamRefB = new MockStreamRef(futB);
		mockListener.addMockStreamToReturn(mockStreamRefB);
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
		
		Assert.assertFalse(mockStreamRefA.isCancelled());
		Assert.assertFalse(mockStreamRefB.isCancelled());
		
		mockChannel.simulateClose();
		
		Assert.assertFalse(mockStreamRefA.isCancelled()); //this request is done, nothing to cancel
		Assert.assertTrue(mockStreamRefB.isCancelled());		
	}
}
