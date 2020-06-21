package org.webpieces.httpfrontend2.api.http2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class TestInvalidPreface extends AbstractFrontendHttp2Test {

	protected void simulateClientSendingPrefaceAndSettings() {
		//do in the test
	}
	
	@Test
	public void testBadPreface() {
		String preface = "PRI * HTTP/INVALID CONNECTION PREFACE\r\n\r\nSM\r\n\r\n";
		byte[] bytes = preface.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		mockChannel.send(buffer);
		
		Assert.assertTrue(mockChannel.isClosed());
	}
}
