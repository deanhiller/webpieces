package org.webpieces.plugins.documentation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
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

public class TestDocs extends AbstractWebpiecesTest {
	
	private MockBackendLogin backendLogin = new MockBackendLogin();
	private HttpSocket https11Socket;
	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("documentationMeta.txt", TestDocs.class.getClassLoader());
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
			binder.bind(BackendLogin.class).toInstance(backendLogin);
		}
	}
	
	@Test
	public void testLoginPageRender() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/@documentation");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);		
		response.assertContains("Quick Start");
	}

}

