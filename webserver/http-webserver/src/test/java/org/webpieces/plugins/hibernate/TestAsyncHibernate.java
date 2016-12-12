package org.webpieces.plugins.hibernate;

import java.util.List;
import java.util.concurrent.Executor;

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
import org.webpieces.jdbc.api.JdbcApi;
import org.webpieces.jdbc.api.JdbcFactory;
import org.webpieces.mock.lib.MockExecutor;
import org.webpieces.plugins.hsqldb.H2DbPlugin;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class TestAsyncHibernate {
	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	private MockExecutor mockExecutor = new MockExecutor();
	
	@Before
	public void setUp() {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		
		List<WebAppMeta> plugins = Lists.newArrayList(
				new HibernatePlugin(HibernateModule.PERSISTENCE_TEST_UNIT), 
				new H2DbPlugin());

		mockExecutor.clear();
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(new PlatformOverridesForTest());
		config.setAppOverrides(new TestOverrides());
		config.setMetaFile(metaFile);
		config.setPlugins(plugins);
		WebserverForTest webserver = new WebserverForTest(config);
		server = webserver.start();
	}

	private String saveBean(String path) {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.POST, path);
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses1 = socket.getResponses();
		Assert.assertEquals(0, responses1.size());
		List<Runnable> runnables = mockExecutor.getRunnablesScheduled();
		runnables.get(0).run();
		mockExecutor.clear();
		
		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(1, responses2.size());

		FullResponse response = responses2.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		socket.clear();
		
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.LOCATION);
		String url = header.getValue();
		return url;
	}
	
	private void readBean(String redirectUrl, String email) {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, redirectUrl);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses1 = socket.getResponses();
		Assert.assertEquals(0, responses1.size());
		List<Runnable> runnables = mockExecutor.getRunnablesScheduled();
		runnables.get(0).run();
		
		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(1, responses2.size());

		FullResponse response = responses2.get(0);
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
		Integer id = TestSyncHibernate.loadDataInDb();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/async/dynamic/"+id);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses1 = socket.getResponses();
		Assert.assertEquals(0, responses1.size());
		List<Runnable> runnables = mockExecutor.getRunnablesScheduled();
		runnables.get(0).run();
		
		List<FullResponse> responses2 = socket.getResponses();
		Assert.assertEquals(1, responses2.size());

		FullResponse response = responses2.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}
	
	@Test
	public void testOptimisticLock() {
	}
	
	private class TestOverrides implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(Executor.class).toInstance(mockExecutor);
		}
	}
}
