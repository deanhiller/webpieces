package org.webpieces.plugin.sslcert;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.webpieces.plugin.backend.login.BackendLogin;
import org.webpieces.plugin.secure.sslcert.acme.AcmeClientProxy;
import org.webpieces.plugin.secure.sslcert.acme.AcmeInfo;
import org.webpieces.plugins.fortesting.TestConfig;
import org.webpieces.plugins.fortesting.WebserverForTest;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Binder;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;


public class TestSslSetup extends AbstractWebpiecesTest {
	
	private HttpSocket http11Socket;
	private MockBackendLogin backendLogin = new MockBackendLogin();
	private MockSimpleStorage mockStorage = new MockSimpleStorage();
	private HttpSocket https11Socket;
	private MockAcmeClient mockAcmeClient = new MockAcmeClient();
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("sslMeta.txt", TestSslSetup.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(getOverrides(false, new SimpleMeterRegistry()));
		config.setAppOverrides(new SslTestModule());
		config.setMetaFile(metaFile);
		WebserverForTest webserver = new WebserverForTest(config);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
		https11Socket = connectHttps(false, null, webserver.getUnderlyingHttpsChannel().getLocalAddress());		
	}
	
	private class SslTestModule implements com.google.inject.Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(BackendLogin.class).toInstance(backendLogin);
			binder.bind(SimpleStorage.class).toInstance(mockStorage);
			binder.bind(AcmeClientProxy.class).toInstance(mockAcmeClient);
		}
	}
	
	@Test
	public void testAccessPageWillRedirectToLogin() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/@sslcert");
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		
		//redirect to the login page in the backend plugin...
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.LOCATION);
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("https://myhost.com/@backend/login", headers.get(0).getValue());
	}
	
	@Test
	public void testAlreadyLoggedInAndFirstTimeNoProperties() throws MalformedURLException {
		URI terms = URI.create("http://somerandom.com/place");
		URL website = new URL("http://website.com");
		
		mockStorage.addReadResponse(CompletableFuture.completedFuture(new HashMap<>()));
		
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/@sslcert");
		//response from logging in taken from TestLogin in backend plugin test suite
		//set-cookie: webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin; path=/; HttpOnly
		req.addHeader(new Header(KnownHeaderName.COOKIE, "webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin"));
		
		mockAcmeClient.setRemoteInfo(CompletableFuture.completedFuture(new AcmeInfo(terms, website)));

		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		
		//redirect to the login page in the backend plugin...
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("You Must agree to the terms found at");
	}

	//@Test
	public void testAlreadyLoggedInAndAlreadyHavePropertiesSetup() {
		
	}

	@Test
	public void testPostEmailNotLoggedInBecauseeNoCookie() {
		HttpFullRequest req = Requests.createPostRequest( "/@sslcert/postEmail", 
				"email", "dean@gmail.com"
			);
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.LOCATION);
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("https://myhost.com/@backend/login", headers.get(0).getValue());
	}

}

