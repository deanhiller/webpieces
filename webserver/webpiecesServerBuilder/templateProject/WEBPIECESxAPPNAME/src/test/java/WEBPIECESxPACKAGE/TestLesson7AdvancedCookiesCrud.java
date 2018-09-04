package WEBPIECESxPACKAGE;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.WebBrowserSimulator;
import org.webpieces.webserver.test.http11.Requests;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 * 
 * @author dhiller
 *
 */
public class TestLesson7AdvancedCookiesCrud extends AbstractWebpiecesTest {

	private final static Logger log = LoggerFactory.getLogger(TestLesson7AdvancedCookiesCrud.class);
	
	private JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);

	private WebBrowserSimulator webBrowser;
	private static String pUnit = HibernatePlugin.PERSISTENCE_TEST_UNIT;
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		log.info("Setting up test");
		Asserts.assertWasCompiledWithParamNames("test");
		
		//clear in-memory database
		jdbc.dropAllTablesFromDatabase();
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		Server webserver = new Server(getOverrides(false), null, new ServerConfig(pUnit, JavaCache.getCacheLocation()));
		webserver.start();
		HttpSocket https11Socket = connectHttps(false, null, webserver.getUnderlyingHttpChannel().getLocalAddress());
		webBrowser = new WebBrowserSimulator(https11Socket);
	}
	
	/**
	 * If a user types in a form but was logged out, we keep that data in flash until logged in
	 * so the user experience is better and we don't just blow away all his data that he just
	 * typed in from the simple mistake of not being logged in.
	 * 
	 * This is a test that runs during your build BUT also runs in the webpieces build to make sure
	 * we do not break that very nice user experience.
	 */
	@Test
	public void testLoggedOutPostLoginAndFormStillRendersPreviousData() {
		ResponseWrapper postFormResponse = postForm();
		
		ResponseWrapper loginGetResponse = followRedirectToLoginGet(postFormResponse);

		ResponseWrapper loginPostResponse = postLogin(loginGetResponse);
		
		getForm(loginPostResponse);
	}

	private void getForm(ResponseWrapper loginPostResponse) {
		String redirectUrl = loginPostResponse.getRedirectUrl();
		
        HttpFullRequest getFormAfterLoginReq = Requests.createRequest(KnownHttpMethod.GET, redirectUrl);
        
        ResponseWrapper getFormResponse = webBrowser.send(getFormAfterLoginReq);

        getFormResponse.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        getFormResponse.assertContains("<input type=`text` name=`entity.firstName` value=`D&amp;D` class=`input-xlarge`>".replace('`', '\"'));
        getFormResponse.assertContains("<input type=`text` name=`entity.lastName` value=`Hiller` class=`input-xlarge`>".replace('`', '\"'));
        getFormResponse.assertContains("<input type=`text` name=`entity.email` value=`dean.hiller@gmail.com` class=`input-xlarge`>".replace('`', '\"'));
        getFormResponse.assertContains("<input type=`password` name=`password` value=`` class=`input-xlarge`>".replace('`', '\"'));        
	}

	private ResponseWrapper postLogin(ResponseWrapper loginGetResponse) {
		String postUrl = "/postLogin";
		loginGetResponse.assertContains(("<form action=`"+postUrl+"` method=`post`").replace('`', '\"'));
		
		HttpFullRequest loginPostReq = Requests.createPostRequest(postUrl, 
				"username", "dean", 
				"password", "");

		ResponseWrapper loginPostResponse = webBrowser.send(loginPostReq);
		loginPostResponse.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		return loginPostResponse;
	}

	private ResponseWrapper followRedirectToLoginGet(ResponseWrapper postFormResponse) {
		String redirectUrl = postFormResponse.getRedirectUrl();
		
        HttpFullRequest loginReq = Requests.createRequest(KnownHttpMethod.GET, redirectUrl);
        
        ResponseWrapper loginGetResponse = webBrowser.send(loginReq);

        loginGetResponse.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        //assert the nulls came through
        loginGetResponse.assertContains("<input id=`user` type=`text` name=`username` value=`` class=`input-xlarge`>".replace('`', '\"'));
        loginGetResponse.assertContains("<input type=`password` name=`password` value=`` class=`input-xlarge`>".replace('`', '\"'));

		return loginGetResponse;
	}

	private ResponseWrapper postForm() {
		HttpFullRequest postReq = Requests.createPostRequest("/secure/crud/user/post", 
				"entity.firstName", "D&D", 
				"entity.lastName", "Hiller",
				"entity.email", "dean.hiller@gmail.com",
				"password", "555555");
		
		//NOTE: This sucks BUT we must manually simulate the browser referrer header.
		postReq.addHeader(new Header(KnownHeaderName.REFERER, "https://myhost.com:8443/secure/crud/user/new"));
		
		ResponseWrapper response1 = webBrowser.send(postReq);
		
		response1.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("https://myhost.com/login", response1.getRedirectUrl());
		return response1;
	}
	
	public static HttpFullRequest createRequest(String uri) {
		HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri(uri));
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine );
		req.addHeader(new Header(KnownHeaderName.HOST, "yourdomain.com"));
		
		HttpFullRequest fullReq = new HttpFullRequest(req, null);
		return fullReq;
	}
	
}
