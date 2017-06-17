package org.webpieces.httpfrontend2.api.http1;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.Cancel;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

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
		
		CompletableFuture<Void> fut = mockChannel.sendToSvrAsync(req2);
		Assert.assertFalse(fut.isDone());
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		mockChannel.simulateClose();
		
		List<Cancel> cancels = mockListener.getCancels();
		Assert.assertEquals(in1.stream, cancels.get(0).stream);
	}
}
