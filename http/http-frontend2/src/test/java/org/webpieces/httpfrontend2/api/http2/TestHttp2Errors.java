package org.webpieces.httpfrontend2.api.http2;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.Cancel;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;

import com.webpieces.hpack.api.dto.Http2Headers;

public class TestHttp2Errors extends AbstractHttp2Test {
	
	@Test
	public void testFarEndClosedSocket() throws InterruptedException, ExecutionException {
        MockStreamWriter mockSw = new MockStreamWriter();
		mockListener.addMockStreamToReturn(mockSw );
        MockStreamWriter mockSw2 = new MockStreamWriter();
		mockListener.addMockStreamToReturn(mockSw2 );
		
		Http2Headers request1 = Http2Requests.createRequest(1, true);
		Http2Headers request2 = Http2Requests.createRequest(3, true);

		mockChannel.write(request1);
		PassedIn in1 = mockListener.getSingleRequest();
		mockChannel.write(request2);
		PassedIn in2 = mockListener.getSingleRequest();
		
		mockChannel.simulateClose();
		
		List<Cancel> cancels = mockListener.getCancels();
		Assert.assertEquals(in1.stream, cancels.get(0).stream);
		Assert.assertEquals(in2.stream, cancels.get(1).stream);
	}
}
