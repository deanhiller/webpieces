package org.webpieces.plugin.sslcert;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.shredzone.acme4j.Status;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugin.backend.login.BackendLogin;
import org.webpieces.plugin.secure.sslcert.CertAndSigningRequest;
import org.webpieces.plugin.secure.sslcert.acme.AcmeClientProxy;
import org.webpieces.plugin.secure.sslcert.acme.AcmeInfo;
import org.webpieces.plugin.secure.sslcert.acme.ProxyAuthorization;
import org.webpieces.plugin.secure.sslcert.acme.ProxyOrder;
import org.webpieces.plugins.fortesting.SimpleStorageInMemory;
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


public class TestFullSslSetupWizard extends AbstractWebpiecesTest {
	
	private HttpSocket http11Socket;
	private MockBackendLogin backendLogin = new MockBackendLogin();
	private SimpleStorageInMemory storage = new SimpleStorageInMemory();
	private HttpSocket https11Socket;
	private MockAcmeClient mockAcmeClient = new MockAcmeClient();
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("sslMeta.txt", TestFullSslSetupWizard.class.getClassLoader());
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
			binder.bind(SimpleStorage.class).toInstance(storage);
			binder.bind(AcmeClientProxy.class).toInstance(mockAcmeClient);
		}
	}
	
	@Test
	public void testFullSuccessWizardWalkthrough() throws MalformedURLException {
		renderFirstPage("/@backend/secure/sslsetup");
		postFirstPageAndEmail("/@backend/secure/postEmail");
		renderStep2AskingForOrg("/@backend/secure/step2");
		MockProxyAuthorization proxyAuth = postOrgAndPlaceOrderAndFinalizeOrder("/@backend/secure/postStep2");

		verifyTokenPageSetup(proxyAuth);
	}

	private void verifyTokenPageSetup(MockProxyAuthorization proxyAuth) {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/.well-known/acme-challenge/"+proxyAuth.getToken());

		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		Assert.assertEquals(proxyAuth.getAuthContent(), response.getBodyAsString());
		response.assertContentType("text/plain");
		
		HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/.well-known/acme-challenge/notexisttoken");
		CompletableFuture<HttpFullResponse> respFuture2 = https11Socket.send(req2);
		ResponseWrapper response2 = ResponseExtract.waitResponseAndWrap(respFuture2);
		String url = response2.getRedirectUrl();
		response2.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}

	private void renderFirstPage(String url) throws MalformedURLException {
		URI terms = URI.create("http://somerandom.com/place");
		URL website = new URL("http://website.com");
		mockAcmeClient.setRemoteInfo(CompletableFuture.completedFuture(new AcmeInfo(terms, website)));
		
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, url);
		//response from logging in taken from TestLogin in backend plugin test suite
		//set-cookie: webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin; path=/; HttpOnly
		req.addHeader(new Header(KnownHeaderName.COOKIE, "webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin"));
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		
		//redirect to the login page in the backend plugin...
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("You Must agree to the terms found at");
		response.assertContains(terms+"");
	}

	private void postFirstPageAndEmail(String url) {
		HttpFullRequest req = Requests.createPostRequest(url, 
				"email", "dean@gmail.com"
			);
		req.addHeader(new Header(KnownHeaderName.COOKIE, "webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin"));
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.LOCATION);
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("https://myhost.com/@backend/secure/step2", headers.get(0).getValue());
	}

	private void renderStep2AskingForOrg(String url) {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, url);
		//response from logging in taken from TestLogin in backend plugin test suite
		//set-cookie: webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin; path=/; HttpOnly
		req.addHeader(new Header(KnownHeaderName.COOKIE, "webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin"));
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		
		//redirect to the login page in the backend plugin...
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Organization");
	}

	private MockProxyAuthorization postOrgAndPlaceOrderAndFinalizeOrder(String url) throws MalformedURLException {
		URL accountUrl = new URL("http://someurlfor.com/myexact/account/1234");
		mockAcmeClient.setOpenAccount(CompletableFuture.completedFuture(accountUrl));
		
		List<ProxyAuthorization> proxyAuth = new ArrayList<>();
		MockProxyAuthorization mockProxyAuth = new MockProxyAuthorization("domain.com", Instant.now(), Status.PENDING, new URL("http://somelocation.asdf"), "sometokenforwebdisplay", "authcontent111");
		proxyAuth.add(mockProxyAuth);
		mockAcmeClient.setProxyOrder(CompletableFuture.completedFuture(new ProxyOrder(null, proxyAuth)));
		
		mockAcmeClient.setCertAndSigningRequest(CompletableFuture.completedFuture(new CertAndSigningRequest("fakecsr", new ArrayList<>())));
		
		HttpFullRequest req = Requests.createPostRequest(url, 
				"organization", "DeanCo"
			);
		req.addHeader(new Header(KnownHeaderName.COOKIE, "webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin"));
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.LOCATION);
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("https://myhost.com/@backend/secure/maintainssl", headers.get(0).getValue());
		return mockProxyAuth;
	}

	
//	@Test
//	public void testPostEmail() {
//		HttpFullRequest req = Requests.createPostRequest( "/backend/secure/postEmail", 
//				"email", "dean@gmail.com"
//			);
//		//response from logging in taken from TestLogin in backend plugin test suite
//		//set-cookie: webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin; path=/; HttpOnly
//		req.addHeader(new Header(KnownHeaderName.COOKIE, "webSession=1-xjrs6SeNeSxmJQtaTwM8gDorNiQ=:backendUser=admin"));
//		
//		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
//		
//		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
//		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
//		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.LOCATION);
//		Assert.assertEquals(1, headers.size());
//		Assert.assertEquals("https://myhost.com/backend/asdf", headers.get(0).getValue());
//	}
}

