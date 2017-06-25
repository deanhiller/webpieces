package org.webpieces.plugins.hibernate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.mock.lib.MockExecutor;
import org.webpieces.plugins.hibernate.app.ServiceToFail;
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.ResponseExtract;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestAsyncHibernate extends AbstractWebpiecesTest {

	
	private MockExecutor mockExecutor = new MockExecutor();
	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		
		mockExecutor.clear();
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(platformOverrides);
		config.setAppOverrides(new TestOverrides());
		config.setMetaFile(metaFile);
		WebserverForTest webserver = new WebserverForTest(config);
		webserver.start();
		http11Socket = createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	private String saveBean(String path) {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.POST, path);
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());
		List<Runnable> runnables = mockExecutor.getRunnablesScheduled();
		runnables.get(0).run();
		mockExecutor.clear();
		
		FullResponse response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.LOCATION);
		String url = header.getValue();
		return url;
	}
	
	private void readBean(String redirectUrl, String email) {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, redirectUrl);

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());
		List<Runnable> runnables = mockExecutor.getRunnablesScheduled();
		runnables.get(0).run();

		FullResponse response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("name=SomeName email="+email);
	}
	
	@Test 
	public void testAsyncWithFilter() {
		String redirectUrl = saveBean("/async/save");
		readBean(redirectUrl, "dean@async.xsoftware.biz");		
	}
	
	/**
	 * Tests when we load user but not company, user.company.name in the page will blow up.
	 * Database loads must be done in the controllers
	 * 
	 */
	@Test
	public void testDbUseWhileRenderingPage() {
		Integer id = TestSyncHibernate.loadDataInDb().getId();
		TestSyncHibernate.verifyLazyLoad(id);
		
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/async/dynamic/"+id);

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		Assert.assertFalse(respFuture.isDone());
		List<Runnable> runnables = mockExecutor.getRunnablesScheduled();
		runnables.get(0).run();
		
		FullResponse response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}
	
	@Test
	public void testOptimisticLock() {
	}
	
	private class TestOverrides implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(Executor.class).toInstance(mockExecutor);
			binder.bind(ServiceToFail.class).toInstance(new ServiceToFailMock());
		}
	}
}
