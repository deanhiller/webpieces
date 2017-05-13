package org.webpieces.httpfrontend2.api.http1;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;


public class TestHttp11Basic extends AbstractHttp1Test {
	
	@Test
	public void testUploadWithChunking() {
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
	public void testUploadWithBody() {
		String bodyStr = "hi there, how are you";
		DataWrapper dataWrapper = dataGen.wrapByteArray(bodyStr.getBytes(StandardCharsets.UTF_8));
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		req.setBody(dataWrapper);
		req.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, ""+dataWrapper.getReadableSize()));

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		HttpRequest req1 = Http2Translations.translateRequest(in1.request);
		Assert.assertEquals(req, req1);

		DataFrame frame = (DataFrame) mockStreamWriter.getSingleFrame();
		DataWrapper data = frame.getData();
		Assert.assertEquals(bodyStr, data.createStringFromUtf8(0, data.getReadableSize()));
		Assert.assertTrue(frame.isEndOfStream());
		
		
		HttpResponse resp = Requests.createResponse();
		Http2Headers http2Resp = (Http2Headers) Http2Translations.translate(resp, false);
		in1.stream.sendResponse(http2Resp);
		
		HttpResponse respToClient = (HttpResponse) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, respToClient);
	}

	@Test
	public void testSendTwoRequestsAndMisorderedResponses() throws InterruptedException, ExecutionException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/xxxx");
		
		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		mockChannel.write(req2);
		PassedIn in2 = mockListener.getSingleRequest();
//		
//		//send back request2's response first!!!! BUT verify it does not go to client per http11 pipelining rules
//		in2.stream.sendResponse(Responses.createResponse(2));
//
//		//assert NOT received
//		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
//		
//		in1.stream.sendResponse(Responses.createResponse(1));
//		
//		List<HttpMessage> msgs = mockChannel.getFramesAndClear();
//		Assert.assertEquals(2,  msgs);
//		
//		HttpResponse resp1 = (HttpResponse) msgs.get(0);
//		HttpResponse resp2 = (HttpResponse) msgs.get(1);
//		
//		Header header = resp1.getHeaderLookupStruct().getHeader(KnownHeaderName.SERVER);
//		Assert.assertEquals("1", header.getValue());
//		
//		Header header2 = resp2.getHeaderLookupStruct().getHeader(KnownHeaderName.SERVER);
//		Assert.assertEquals("2", header2.getValue());
	}


}
