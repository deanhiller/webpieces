package org.webpieces.webserver;

import java.util.List;

import org.junit.Assert;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;

public class ResponseExtract {

	public static FullResponse assertSingleResponse(MockResponseSender socket) {
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		FullResponse response = responses.get(0);
		socket.clear();
		return response;
	}

}
