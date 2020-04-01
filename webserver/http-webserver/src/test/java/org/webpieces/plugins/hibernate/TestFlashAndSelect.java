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
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;
import org.webpieces.plugins.hibernate.app.dbo.Role;
import org.webpieces.plugins.hibernate.app.dbo.UserRoleDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateTestConfig;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;



public class TestFlashAndSelect extends AbstractWebpiecesTest {

	
	private ServiceToFailMock mock = new ServiceToFailMock();
	private UserTestDbo user;
	private HttpSocket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		user = loadDataInDb();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateTestConfig config = new PrivateTestConfig();
		config.setPlatformOverrides(getOverrides(false, new SimpleMeterRegistry()));
		config.setMetaFile(metaFile);
		config.setAppOverrides(new TestModule(mock));
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(config);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testAssertBeanNoNullsOnLastNameAndEnum() {
		String urlPath = "/user/edit/"+user.getId();
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        
        CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        response.assertContains("<input type=`text` name=`entity.lastName` value=`Hill` class=`input-xlarge`>".replace('`', '\"'));
        response.assertContains("<option value=`` >Unselected</option>".replace('`', '\"'));
        response.assertContains("<option value=`k` selected=`selected`>Kindergarten</script>".replace('`', '\"'));
	}
	
	@Test
	public void testNullWillFlashProperly() {
		HttpFullRequest req1 = Requests.createPostRequest("/user/post", 
				"entity.id", user.getId()+"",
				"entity.firstName", "NextName", //invalid first name
				"entity.email", "dean@zz.com",
				"entity.lastName", "",
				"entity.password", "",
				"entity.levelOfEducation", ""
				);
		
		CompletableFuture<HttpFullResponse> respFuture1 = http11Socket.send(req1);
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture1);
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		String urlPath = "/user/edit/"+user.getId();
		Assert.assertEquals("http://myhost.com"+urlPath, response1.getRedirectUrl());
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        response.assertContains("<input type=`text` name=`entity.lastName` value=`` class=`input-xlarge`>".replace('`', '\"'));
        response.assertContains("<option value=`` selected=`selected`>Unselected</option>".replace('`', '\"'));
        response.assertContains("<option value=`k` >Kindergarten</script>".replace('`', '\"'));
	}

	@Test
	public void testRenderGetMultiselect() {
		String urlPath = "/multiselect/"+user.getId();
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        
        CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        response.assertContains("<option value=`b` selected=`selected`>Badass</script>".replace('`', '\"'));
        response.assertContains("<option value=`j` >Jerk</script>".replace('`', '\"'));
        response.assertContains("<option value=`d` selected=`selected`>Delinquint</script>".replace('`', '\"'));
	}
	
	@Test
	public void testMultiSelect() {
		HttpFullRequest req1 = Requests.createPostRequest("/multiselect", 
				"entity.id", user.getId()+"",
				"entity.firstName", "NextName", //invalid first name
				"entity.email", "dean@zz.com",
				"entity.lastName", "",
				"entity.password", "",
				"entity.levelOfEducation", "",
				"selectedRoles", "j",
				"selectedRoles", "d"
				);
		
		CompletableFuture<HttpFullResponse> respFuture1 = http11Socket.send(req1);
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture1);
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		String urlPath = "/multiselect/"+user.getId();
		Assert.assertEquals("http://myhost.com"+urlPath, response1.getRedirectUrl());
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("<option value=`b` >Badass</script>".replace('`', '\"'));
        response.assertContains("<option value=`j` selected=`selected`>Jerk</script>".replace('`', '\"'));
        response.assertContains("<option value=`d` selected=`selected`>Delinquint</script>".replace('`', '\"'));
	}
	
	@Test
	public void testMultiSelectSingleSelection() {
		HttpFullRequest req1 = Requests.createPostRequest("/multiselect", 
				"entity.id", user.getId()+"",
				"entity.firstName", "NextName", //invalid first name
				"entity.email", "dean@zz.com",
				"entity.lastName", "",
				"entity.password", "",
				"entity.levelOfEducation", "",
				"selectedRoles", "j"
				);
		
		CompletableFuture<HttpFullResponse> respFuture1 = http11Socket.send(req1);
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture1);
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		String urlPath = "/multiselect/"+user.getId();
		Assert.assertEquals("http://myhost.com"+urlPath, response1.getRedirectUrl());
        HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);

        ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("<option value=`b` >Badass</script>".replace('`', '\"'));
        response.assertContains("<option value=`j` selected=`selected`>Jerk</script>".replace('`', '\"'));
        response.assertContains("<option value=`d` >Delinquint</script>".replace('`', '\"'));
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
		
		UserRoleDbo role1 = new UserRoleDbo(user, Role.DELINQUINT);
		UserRoleDbo role2 = new UserRoleDbo(user, Role.BADASS);

		mgr.persist(manager);
		mgr.persist(user);

		mgr.persist(role1);
		mgr.persist(role2);

		mgr.flush();
		
		tx.commit();
		return user;
	}
	
}
