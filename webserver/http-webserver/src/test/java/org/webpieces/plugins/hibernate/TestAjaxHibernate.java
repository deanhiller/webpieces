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
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.app.HibernateAppMeta;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;
import org.webpieces.webserver.test.Http11FullRequest;

public class TestAjaxHibernate extends AbstractWebpiecesTest {

	
	private Http11Socket http11Socket;
	
	@Before
	public void setUp() {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(platformOverrides);
		config.setAppOverrides(new TestModule());
		config.setMetaFile(metaFile);
		WebserverForTest webserver = new WebserverForTest(config);
		webserver.start();
		http11Socket = http11Simulator.openHttp();
	}
	
	@Test
	public void testNotFoundInSubRoute() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/ajax/notfound");

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);

		response.assertContains("Your page was not found");
	}

	@Test
	public void testAjaxAddUser() {
		Http11FullRequest req = Requests.createPostRequest("/ajax/user/post",
				"entity.id", "",
				"entity.name", "blah1",
				"entity.firstName", "blah2",
				"password", "asddd");

		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/ajax/user/list", response.getRedirectUrl());
		
		Header header = response.createCookieRequestHeader();
		Assert.assertTrue("contents actually was="+header.getValue(), 
				header.getValue().contains("User+successfully+saved"));
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

	
}
