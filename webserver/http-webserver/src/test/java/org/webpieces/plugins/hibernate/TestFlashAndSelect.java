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
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.app.HibernateAppMeta;
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;
import org.webpieces.plugins.hibernate.app.dbo.Role;
import org.webpieces.plugins.hibernate.app.dbo.UserRoleDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.TestConfig;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class TestFlashAndSelect extends AbstractWebpiecesTest {

	
	private ServiceToFailMock mock = new ServiceToFailMock();
	private UserTestDbo user;
	private Http11Socket http11Socket;

	@Before
	public void setUp() {
		//clear in-memory database
		JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
		jdbc.dropAllTablesFromDatabase();
		user = loadDataInDb();
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		TestConfig config = new TestConfig();
		config.setPlatformOverrides(platformOverrides);
		config.setMetaFile(metaFile);
		config.setAppOverrides(new TestModule(mock));
		WebserverForTest webserver = new WebserverForTest(config);
		webserver.start();
		http11Socket = http11Simulator.openHttp();
	}

	@Test
	public void testAssertBeanNoNullsOnLastNameAndEnum() {
		String urlPath = "/user/edit/"+user.getId();
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        
        http11Socket.send(req);

        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        response.assertContains("<input type=`text` name=`entity.lastName` value=`Hill` class=`input-xlarge`>".replace('`', '\"'));
        response.assertContains("<option value=`` >Unselected</option>".replace('`', '\"'));
        response.assertContains("<option value=`k` selected=`selected`>Kindergarten</script>".replace('`', '\"'));
	}
	
	@Test
	public void testNullWillFlashProperly() {
		HttpRequest req1 = Requests.createPostRequest("/user/post", 
				"entity.id", user.getId()+"",
				"entity.firstName", "NextName", //invalid first name
				"entity.email", "dean@zz.com",
				"entity.lastName", "",
				"entity.password", "",
				"entity.levelOfEducation", ""
				);
		
		http11Socket.send(req1);
		
		FullResponse response1 = ResponseExtract.assertSingleResponse(http11Socket);
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		String urlPath = "/user/edit/"+user.getId();
		Assert.assertEquals("http://myhost.com"+urlPath, response1.getRedirectUrl());
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        http11Socket.send(req);

        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        response.assertContains("<input type=`text` name=`entity.lastName` value=`` class=`input-xlarge`>".replace('`', '\"'));
        response.assertContains("<option value=`` selected=`selected`>Unselected</option>".replace('`', '\"'));
        response.assertContains("<option value=`k` >Kindergarten</script>".replace('`', '\"'));
	}

	@Test
	public void testRenderGetMultiselect() {
		String urlPath = "/multiselect/"+user.getId();
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        
        http11Socket.send(req);

        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        response.assertContains("<option value=`b` selected=`selected`>Badass</script>".replace('`', '\"'));
        response.assertContains("<option value=`j` >Jerk</script>".replace('`', '\"'));
        response.assertContains("<option value=`d` selected=`selected`>Delinquint</script>".replace('`', '\"'));
	}
	
	@Test
	public void testMultiSelect() {
		HttpRequest req1 = Requests.createPostRequest("/multiselect", 
				"entity.id", user.getId()+"",
				"entity.firstName", "NextName", //invalid first name
				"entity.email", "dean@zz.com",
				"entity.lastName", "",
				"entity.password", "",
				"entity.levelOfEducation", "",
				"selectedRoles", "j",
				"selectedRoles", "d"
				);
		
		http11Socket.send(req1);
		
		FullResponse response1 = ResponseExtract.assertSingleResponse(http11Socket);
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		String urlPath = "/multiselect/"+user.getId();
		Assert.assertEquals("http://myhost.com"+urlPath, response1.getRedirectUrl());
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        http11Socket.send(req);

        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);

        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("<option value=`b` >Badass</script>".replace('`', '\"'));
        response.assertContains("<option value=`j` selected=`selected`>Jerk</script>".replace('`', '\"'));
        response.assertContains("<option value=`d` selected=`selected`>Delinquint</script>".replace('`', '\"'));
	}
	
	@Test
	public void testMultiSelectSingleSelection() {
		HttpRequest req1 = Requests.createPostRequest("/multiselect", 
				"entity.id", user.getId()+"",
				"entity.firstName", "NextName", //invalid first name
				"entity.email", "dean@zz.com",
				"entity.lastName", "",
				"entity.password", "",
				"entity.levelOfEducation", "",
				"selectedRoles", "j"
				);
		
		http11Socket.send(req1);
		
		FullResponse response1 = ResponseExtract.assertSingleResponse(http11Socket);
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		String urlPath = "/multiselect/"+user.getId();
		Assert.assertEquals("http://myhost.com"+urlPath, response1.getRedirectUrl());
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, urlPath);
        Header cookieHeader = response1.createCookieRequestHeader();
        req.addHeader(cookieHeader);
        
        http11Socket.send(req);

        FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);

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
