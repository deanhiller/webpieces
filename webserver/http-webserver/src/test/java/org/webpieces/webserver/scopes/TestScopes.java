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
		VirtualFileClasspath metaFile = new VirtualFileClasspath("scopesMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
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

    @Test
    public void testValidationError() {
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/validationerror");
        server.incomingRequest(req, new RequestId(0), true, socket);

        List<FullResponse> responses = socket.getResponses();
        Assert.assertEquals(1, responses.size());

        FullResponse response = responses.get(0);
        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("Err: it failed");
    }
    
	@Test
    public void testFlashMessage() {
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/flashmessage");
        server.incomingRequest(req, new RequestId(0), true, socket);

        List<FullResponse> responses = socket.getResponses();
        Assert.assertEquals(1, responses.size());

        FullResponse response = responses.get(0);
        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("Msg: it worked");
    }
    
	//we hit an issue where validation errors are sticking around so create a test here and
	//fix it.
	//need to test out validation errors don't stick around or validation.hasErrors will stay forever true
	//even when entity is finally ok
	@Test
	public void testValidationErrorDoesNotPersist() {
		//POST first resulting in redirect with errors
		//RENDER page WITH errors, verify errors are there
		//POST again with valid data and verify POST succeeds
		
		FullResponse response1 = runInvalidPost();
		FullResponse response2 = runGetUserFormWithErrors(response1);
		
		Header header = response2.createCookieRequestHeader();
		HttpRequest req = Requests.createPostRequest("/user/post", 
				"user.id", "",
				"user.firstName", "Dean", //valid firstname
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");		
		req.addHeader(header);

		//Did not need to finish test right now
	}

	private FullResponse runInvalidPost() {
		HttpRequest req = Requests.createPostRequest("/user/post", 
				"user.firstName", "D", //invalid first name
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		socket.clear();
		return response;
	}
	
	private FullResponse runGetUserFormWithErrors(FullResponse response1) {
		Assert.assertEquals("http://myhost.com/user/new", response1.getRedirectUrl());
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/new");
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        server.incomingRequest(req, new RequestId(0), true, socket);

        List<FullResponse> responses = socket.getResponses();
        Assert.assertEquals(1, responses.size());

        FullResponse response = responses.get(0);
        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("First name must be more than 2 characters");
        socket.clear();
		return response;
	}
}
