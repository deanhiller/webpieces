package org.webpieces.httpfrontend2.api.http1;

import java.util.List;

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

		mockChannel.write(req);		
		PassedIn in1 = mockListener.getSingleRequest();
		
		mockChannel.write(req2);
		PassedIn in2 = mockListener.getSingleRequest();
		
		mockChannel.simulateClose();
		
		List<Cancel> cancels = mockListener.getCancels();
		Assert.assertEquals(in1.stream, cancels.get(0).stream);
		Assert.assertEquals(in2.stream, cancels.get(1).stream);
	}
}
