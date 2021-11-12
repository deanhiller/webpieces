package org.webpieces.webserver.scopes;

import org.junit.After;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.security.Security;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;



public class TestScopes extends AbstractWebpiecesTest {

	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		Assert.assertEquals(new HashMap<>(), Context.getContext()); //validate nothing here first

		VirtualFileClasspath metaFile = new VirtualFileClasspath("scopesMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false, new SimpleMeterRegistry()), null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());

		//not exactly part of this test but checking for leak of server context into client
		// (only in embedded modes does this occur)
		Assert.assertEquals(new HashMap<>(), Context.getContext());
	}

	@After
	public void tearDown() {
		Context.clear();
	}

	@Test
	public void testSessionScope() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("age=30");
		response.assertContains("result=true");
		response.assertContains("name=Dean");
	}

	@Test
	public void testSessionScopeModificationByClient() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");

		//not exactly part of this test but noticed an issue of context leak from server to client in embedded mode that should not exist
		Assert.assertEquals(0, Context.getContext().size());

		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);

		//not exactly part of this test but noticed an issue of context leak from server to client in embedded mode that should not exist
		Assert.assertEquals(0, Context.getContext().size());

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture1);

		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("age=30");
		
		Header cookie = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
		String value = cookie.getValue();
		value = value.replace("age=30", "age=60");
		value = value.replace("; path=/; HttpOnly", "");
		
		//modify cookie and rerequest...
		HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/displaySession");
		req2.addHeader(new Header(KnownHeaderName.COOKIE, value));
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req2);
		
        response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		Header cookie2 = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
		String deleteCookie = cookie2.getValue();
		Assert.assertEquals("webSession=; Max-Age=0; path=/; HttpOnly", deleteCookie);
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.LOCATION);
		String url = header.getValue();
		Assert.assertEquals("http://myhost.com/displaySession", url);
	}

	@Test
    public void testFlashMessage() {
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/flashmessage");
        XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        
        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("Msg: it worked");
    }

	@Test
    public void testGetStaticFileDoesNotClearFlashMessage() {
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/flashmessage");
        XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture1);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        
		Header header = response.createCookieRequestHeader();
        HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/public/fonts.css");
        req2.addHeader(header);
        XFuture<HttpFullResponse> respFuture = http11Socket.send(req2);
        
        ResponseWrapper response2 = ResponseExtract.waitResponseAndWrap(respFuture);
        response2.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        Header cookie = response2.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.SET_COOKIE);
        Assert.assertNull("static routes should not be clearing cookies or things go south", cookie);

    }
	
	//A basic POST form with invalid field, redirect to error page and load AND THEN
	//POST form with valid data and expect success redirect
	//This tests out the Validation scoped cookie
	@Test
	public void testValidationErrorDoesNotPersist() {
		//POST first resulting in redirect with errors
		//RENDER page WITH errors, verify errors are there
		//POST again with valid data and verify POST succeeds
		
		ResponseWrapper response1 = runInvalidPost();
		ResponseWrapper response2 = runGetUserFormWithErrors(response1);
		
		Header header = response2.createCookieRequestHeader();
		HttpFullRequest req = Requests.createPostRequest("/user/post", 
				"user.id", "",
				"user.firstName", "Dean", //valid firstname
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");		
		req.addHeader(header);

		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/user/list", response.getRedirectUrl());
	}

	private ResponseWrapper runInvalidPost() {
		HttpFullRequest req = Requests.createPostRequest("/user/post", 
				"user.firstName", "D", //invalid first name
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		return response;
	}
	
	private ResponseWrapper runGetUserFormWithErrors(ResponseWrapper response1) {
		Assert.assertEquals("http://myhost.com/user/new", response1.getRedirectUrl());
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/new");
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("First name must be more than 2 characters");
		return response;
	}
}
