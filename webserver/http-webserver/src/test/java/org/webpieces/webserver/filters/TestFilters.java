package org.webpieces.webserver.filters;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.filters.app.Remote;
import org.webpieces.webserver.https.MockRemote;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.AbstractModule;

public class TestFilters {

	private RequestListener server;
	private MockResponseSender socket = new MockResponseSender();
	private MockRemote mockRemote = new MockRemote();

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("filtersMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), new AppOverrides(), false, metaFile);
		server = webserver.start();
	}
	
	@Test
	public void testFilterOrderAndUniqueInit() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/test/something");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
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
