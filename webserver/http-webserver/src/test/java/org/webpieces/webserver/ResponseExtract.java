package org.webpieces.webserver;

import java.util.List;

import org.junit.Assert;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class ResponseExtract {

	public static FullResponse assertSingleResponse(Http11Socket http11Socket) {
		List<FullResponse> responses = http11Socket.getResponses();
		Assert.assertEquals(1, responses.size());
		FullResponse response = responses.get(0);
		http11Socket.clear();
		return response;
	}

}
