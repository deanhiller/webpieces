package org.webpieces.webserver.beans;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.biz.SomeOtherLib;
import org.webpieces.webserver.basic.biz.UserDbo;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestBeans {

	private HttpRequestListener server;
	private MockFrontendSocket socket = new MockFrontendSocket();
	private MockSomeOtherLib mockLib = new MockSomeOtherLib();

	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		VirtualFileClasspath metaFile = new VirtualFileClasspath("beansMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), new AppOverridesModule(), false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testPageParam() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/pageparam");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Hi Dean Hiller, this is testing");
	}

	@Test
	public void testComplexBean() {
		HttpRequest req = Requests.createPostRequest("/postuser", 
				"user.firstName", "D&D", 
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");
		
		server.processHttpRequests(socket, req , false);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserDbo user = mockLib.getUser();
		Assert.assertEquals(555, user.getAddress().getZipCode());
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals("Coolness Dr.", user.getAddress().getStreet());
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockLib);
		}
	}
}
