package org.webpieces.webserver.scopes;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestScopes {

	private RequestListener server;
	private MockResponseSender socket = new MockResponseSender();
	
	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig();
		VirtualFileClasspath metaFile = new VirtualFileClasspath("scopesMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(config), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testSessionScope() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("age=30");
		response.assertContains("result=true");
		response.assertContains("name=Dean");
	}

	@Test
	public void testSessionScopeModificationByClient() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("age=30");
		
		socket.clear();
		
		Header cookie = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
		String value = cookie.getValue();
		value = value.replace("age=30", "age=60");
		value = value.replace("; path=/; HttpOnly", "");
		
		//modify cookie and rerequest...
		HttpRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/displaySession");
		req2.addHeader(new Header(KnownHeaderName.COOKIE, value));
		
		server.incomingRequest(req2, new RequestId(0), true, socket);
		
		responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		Header cookie2 = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
		String deleteCookie = cookie2.getValue();
		Assert.assertEquals("webSession=; Max-Age=0; path=/; HttpOnly", deleteCookie);
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.LOCATION);
		String url = header.getValue();
		Assert.assertEquals("http://myhost.com/displaySession", url);
	}

}
