package org.webpieces.plugins.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.app.HibernateAppMeta;
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;


public class TestSyncHibernate extends AbstractWebpiecesTest {

	
	private ServiceToFailMock mock = new ServiceToFailMock();
	private Http11Socket http11Socket;

	@Before
	public void setUp() {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(platformOverrides);
		config.setMetaFile(metaFile);
		config.setAppOverrides(new TestModule(mock));
		WebserverForTest webserver = new WebserverForTest(config);
		webserver.start();
		http11Socket = http11Simulator.createHttpSocket(webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	private String saveBean(String path) {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.POST, path);
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		return response.getRedirectUrl();
	}
	
	private void readBean(String redirectUrl, String email) {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, redirectUrl);

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
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
		verifyLazyLoad(id);
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/dynamic/"+id);

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}

	public static UserTestDbo loadDataInDb() {
		String email = "dean2@sync.xsoftware.biz";
		//populate database....
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(HibernateAppMeta.PERSISTENCE_TEST_UNIT);
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		UserTestDbo manager = new UserTestDbo();
		manager.setEmail("asdf@asf.com");
		manager.setName("somadsf");
		
		UserTestDbo user = new UserTestDbo();
		user.setEmail(email);
		user.setName("SomeName");
		user.setFirstName("Dean");
		user.setLastName("Hill");
		user.setManager(manager);
		
		mgr.persist(manager);
		mgr.persist(user);

		mgr.flush();
		
		tx.commit();
		return user;
	}
	
	public static void verifyLazyLoad(int id) {
		//verify lazy load is working so we know test is testing what it should be
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(HibernateAppMeta.PERSISTENCE_TEST_UNIT);
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		UserTestDbo user = mgr.find(UserTestDbo.class, id);
		UserTestDbo manager = user.getManager();

		Assert.assertEquals("somadsf", manager.getName());

		mgr.flush();
		
		tx.commit();
	}
	
	@Test
	public void testReverseAddAndEditFromRouteId() {
		loadDataInDb();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/list");

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<a href=`/user/new`>Add User</a>".replace("`", "\""));
		response.assertContains("<a href=`/user/edit/1`>Edit</a>".replace("`", "\""));
	}

	@Test
	public void testRenderAddPage() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/new");

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("name='' email=''");
	}

	@Test
	public void testRenderEditPage() {
		int id = loadDataInDb().getId();
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/edit/"+id);

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("name='SomeName' email='dean2@sync.xsoftware.biz'");
	}
	
	@Test
	public void testHibernatePostPartialDataDoesntBlowDataAway() {
		UserTestDbo user = loadDataInDb();
		HttpFullRequest req = Requests.createPostRequest("/testmerge",
				"user.id", user.getId()+"",
				"user.name", "blah1",
				"user.firstName", "blah2");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserTestDbo user2 = load(user.getId());
		Assert.assertEquals("blah1", user2.getName()); //name changed
		Assert.assertEquals("blah2", user2.getFirstName()); //firstname changed
		Assert.assertEquals(user.getLastName(), user2.getLastName()); //lastname remained the same
	}

	@Test
	public void testHibernateNoUserIdWillSaveNewUser() {
		String email = "test2";
		HttpFullRequest req = Requests.createPostRequest("/testmerge",
				"user.id", "",
				"user.email", email,
				"user.name", "blah1",
				"user.firstName", "blah2",
				"user.levelOfEducation", LevelEducation.COLLEGE.getDbCode()+"");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserTestDbo user2 = loadByEmail(email);
		Assert.assertEquals("blah1", user2.getName()); //name changed
		Assert.assertEquals("blah2", user2.getFirstName()); //firstname changed
		Assert.assertEquals(LevelEducation.COLLEGE, user2.getLevelOfEducation());
	}
	
	@Test
	public void testHibernateNoUserIdParamWillSaveNewUser() {
		String email = "test1";
		HttpFullRequest req = Requests.createPostRequest("/testmerge",
				"user.email", email,
				"user.name", "blah1",
				"user.firstName", "blah2");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserTestDbo user2 = loadByEmail(email);
		Assert.assertEquals("blah1", user2.getName()); //name changed
		Assert.assertEquals("blah2", user2.getFirstName()); //firstname changed
	}
	@Test
	public void testRollback() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/fail");

		mock.addException(() -> {throw new RuntimeException("for test");});
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);

		Assert.assertEquals(2, TransactionFilter.getState());
	}
	
	private UserTestDbo loadByEmail(String email) {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(HibernateAppMeta.PERSISTENCE_TEST_UNIT);
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		UserTestDbo user = UserTestDbo.findByEmailId(mgr, email);
		
		tx.commit();
		
		return user;		
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
