package org.webpieces.httpfrontend2.api.http2;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpfrontend2.api.mock2.MockStreamRef;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;

import com.webpieces.hpack.api.dto.Http2Request;

public class TestHttp2Errors extends AbstractHttp2Test {
	
	@Test
	public void testFarEndClosedSocket() throws InterruptedException, ExecutionException {
        MockStreamWriter mockSw = new MockStreamWriter();
        MockStreamRef ref1 = new MockStreamRef(mockSw);
		mockListener.addMockStreamToReturn(ref1 );
        MockStreamWriter mockSw2 = new MockStreamWriter();
        MockStreamRef ref2 = new MockStreamRef(mockSw2);
		mockListener.addMockStreamToReturn(ref2 );
		
		Http2Request request1 = Http2Requests.createRequest(1, true);
		Http2Request request2 = Http2Requests.createRequest(3, true);

		mockChannel.send(request1);
		PassedIn in1 = mockListener.getSingleRequest();
		mockChannel.send(request2);
		PassedIn in2 = mockListener.getSingleRequest();
		
		mockChannel.close();
		
		Assert.assertTrue(ref1.isCancelled());
		Assert.assertTrue(ref2.isCancelled());
	}
}
