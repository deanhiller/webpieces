package org.webpieces.webserver.basic;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class BasicStartWithNoHttps extends AbstractWebpiecesTest {

	@Test
	public void testNoHttps() throws InterruptedException, ExecutionException, TimeoutException {
		String[] arguments = new String[] {"-http.port=:0", "-https.port="};
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), null, null, arguments);
		webserver.start();
		//connecting http still works
		HttpSocket http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
		
		//https has no channel in webserver
		Assert.assertNull(webserver.getUnderlyingHttpsChannel());
	}
}
