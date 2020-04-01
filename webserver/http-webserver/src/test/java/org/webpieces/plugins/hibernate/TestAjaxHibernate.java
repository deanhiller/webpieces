package org.webpieces.plugins.hibernate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.app.HibernateAppMeta;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateTestConfig;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;



public class TestAjaxHibernate extends AbstractWebpiecesTest {


	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateTestConfig config = new PrivateTestConfig();
		config.setPlatformOverrides(getOverrides(false, new SimpleMeterRegistry()));
		config.setAppOverrides(new TestModule());
		config.setMetaFile(metaFile);
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(config);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@Test
	public void testNotFoundInSubRoute() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/ajax/notfound");

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);

		response.assertContains("Your page was not found");
	}

	@Test
	public void testAjaxAddUser() {
		HttpFullRequest req = Requests.createPostRequest("/ajax/user/post",
				"entity.id", "",
				"entity.name", "blah1",
				"entity.firstName", "blah2",
				"password", "asddd");

		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
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
