package org.webpieces.webserver.filters;

import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.filters.app.Remote;
import org.webpieces.webserver.https.MockRemote;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.AbstractModule;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestFilters extends AbstractWebpiecesTest {
	
	
	private MockRemote mockRemote = new MockRemote();
	private HttpSocket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("filtersMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), new AppOverrides(), false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@Test
	public void testFilterOrderAndUniqueInit() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/test/something");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		
		List<Integer> recorded = mockRemote.getRecorded();
		Assert.assertEquals(2, recorded.size());
		Assert.assertEquals(new Integer(1), recorded.get(0));
		Assert.assertEquals(new Integer(2), recorded.get(1));		
	}
	
	private class AppOverrides extends AbstractModule {
		@Override
		protected void configure() {
			bind(Remote.class).toInstance(mockRemote);
		}
	}
}
