package org.webpieces.plugins.backend;

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
import org.webpieces.plugins.backend.login.BackendLogin;
import org.webpieces.plugins.fortesting.TestConfig;
import org.webpieces.plugins.fortesting.WebserverForTest;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Binder;

public class TestLogin extends AbstractWebpiecesTest {
	
	private MockBackendLogin mockLogin = new MockBackendLogin();
	private HttpSocket https11Socket;
	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("backendMeta.txt", TestLogin.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(getOverrides(false));
		config.setAppOverrides(new TestLoginModule());
		config.setMetaFile(metaFile);
		WebserverForTest webserver = new WebserverForTest(config);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
		https11Socket = connectHttps(false, null, webserver.getUnderlyingHttpsChannel().getLocalAddress());		
	}
	
	private class TestLoginModule implements com.google.inject.Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(BackendLogin.class).toInstance(mockLogin);
			//binder.bind(SimpleStorage.class).toInstance(mockStorage);
		}
	}
	
	@Test
	public void testLoginNotExistOnHttp() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/backend/login");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);		
	}
	
	@Test
	public void testLoginPageRender() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/backend/login");
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);		
		response.assertContains("Login");
	}

	@Test
	public void testLoginPost() {
		mockLogin.setAuthentication(true);
		
		HttpFullRequest req = Requests.createPostRequest( "/backend/postLogin", 
				"username", "admin",
				"password", "admin");
		
		CompletableFuture<HttpFullResponse> respFuture = https11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		List<Header> headers = response.getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.LOCATION);
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("https://myhost.com/backend/secure/loggedinhome", headers.get(0).getValue());
	}

}

