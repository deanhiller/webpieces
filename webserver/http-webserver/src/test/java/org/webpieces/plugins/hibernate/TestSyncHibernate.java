package org.webpieces.plugins.hibernate;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.app.HibernateAppMeta;
import org.webpieces.plugins.hibernate.app.dbo.CompanyDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserDbo;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.httpcommon.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestSyncHibernate {
	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("plugins/hibernateMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
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
		
		Header header = response.getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.LOCATION);
		String url = header.getValue();
		return url;
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
		Integer id = loadDataInDb("dean2@sync.xsoftware.biz");
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/dynamic/"+id);

		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}

	public static Integer loadDataInDb(String email) {
		//populate database....
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(HibernateAppMeta.PERSISTENCE_UNIT);
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		CompanyDbo company = new CompanyDbo();
		company.setName("WebPieces LLC");
		
		UserDbo user = new UserDbo();
		user.setEmail(email);
		user.setName("SomeName");
		user.setCompany(company);
		
		mgr.persist(company);
		mgr.persist(user);

		mgr.flush();
		
		tx.commit();
		
		return user.getId();
	}
	
	@Test
	public void testOptimisticLock() {
	}
	
}
