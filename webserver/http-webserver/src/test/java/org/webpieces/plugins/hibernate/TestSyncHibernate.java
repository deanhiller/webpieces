package org.webpieces.plugins.hibernate;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.app.HibernateAppMeta;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestSyncHibernate {
	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(new PlatformOverridesForTest());
		config.setMetaFile(metaFile);
		WebserverForTest webserver = new WebserverForTest(config);
		server = webserver.start();
	}

	private String saveBean(String path) {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.POST, path);
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		socket.clear();
		
		return response.getRedirectUrl();
	}
	
	private void readBean(String redirectUrl, String email) {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, redirectUrl);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("name=SomeName email="+email);
	}
	
	@Test
	public void testSyncWithFilter() {
		String redirectUrl = saveBean("/save");
		readBean(redirectUrl, "dean@sync.xsoftware.biz");
	}
	
	/**
	 * Tests when we load user but not company, user.company.name will blow up since company was not
	 * loaded in the controller from the database.
	 * 
	 * (ie. we only let you traverse the loaded graph so that we don't accidentally have 1+N queries running)
	 */
	@Test
	public void testDbUseWhileRenderingPage() {
		Integer id = loadDataInDb().getId();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/dynamic/"+id);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}

	public static UserTestDbo loadDataInDb() {
		String email = "dean2@sync.xsoftware.biz";
		//populate database....
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(HibernateAppMeta.PERSISTENCE_TEST_UNIT);
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		UserTestDbo user = new UserTestDbo();
		user.setEmail(email);
		user.setName("SomeName");
		user.setFirstName("Dean");
		user.setLastName("Hill");
		
		mgr.persist(user);

		mgr.flush();
		
		tx.commit();
		
		return user;
	}
	
	@Test
	public void testReverseAddAndEditFromRouteId() {
		loadDataInDb();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/list");

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<a href=`/user/new`>Add User</a>".replace("`", "\""));
		response.assertContains("<a href=`/user/edit/1`>Edit</a>".replace("`", "\""));
	}

	@Test
	public void testRenderAddPage() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/new");

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("name='' email=''");
	}

	@Test
	public void testRenderEditPage() {
		int id = loadDataInDb().getId();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/edit/"+id);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("name='SomeName' email='dean2@sync.xsoftware.biz'");
	}
	
	@Ignore
	@Test
	public void testHibernatePostPartialDataDoesntBlowDataAway() {
		UserTestDbo user = loadDataInDb();
		HttpRequest req = Requests.createPostRequest("/testmerge",
				"user.id", user.getId()+"",
				"user.name", "blah1",
				"user.firstName", "blah2");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());
		
		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserTestDbo user2 = load(user.getId());
		Assert.assertEquals("blah1", user2.getName()); //name changed
		Assert.assertEquals("blah2", user2.getFirstName()); //firstname changed
		Assert.assertEquals(user.getLastName(), user2.getLastName()); //lastname remained the same
	}
	
	private UserTestDbo load(Integer id) {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(HibernateAppMeta.PERSISTENCE_TEST_UNIT);
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		UserTestDbo user = mgr.find(UserTestDbo.class, id);
		
		tx.commit();
		
		return user;
	}
	@Test
	public void testOptimisticLock() {
	}
	
}
