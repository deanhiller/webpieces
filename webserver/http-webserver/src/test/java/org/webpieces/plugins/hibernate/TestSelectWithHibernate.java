package org.webpieces.plugins.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.Before;
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
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestSelectWithHibernate {
	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	private ServiceToFailMock mock = new ServiceToFailMock();
	private UserTestDbo user;

	@Before
	public void setUp() {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		user = loadDataInDb();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(new PlatformOverridesForTest());
		config.setMetaFile(metaFile);
		config.setAppOverrides(new TestModule(mock));
		WebserverForTest webserver = new WebserverForTest(config);
		server = webserver.start();
	}

	@Test
	public void testEnumsAndSelect() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/user/edit/"+user.getId());
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<option value=`k` selected=`selected`>Kindergarten</script>".replace('`', '\"'));
		response.assertContains("<option value=`e` >Elementary School</script>".replace('`', '\"'));
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
		user.setLevelOfEducation(LevelEducation.KINDERGARTEN);
		user.setManager(manager);
		
		mgr.persist(manager);
		mgr.persist(user);

		mgr.flush();
		
		tx.commit();
		return user;
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
}
