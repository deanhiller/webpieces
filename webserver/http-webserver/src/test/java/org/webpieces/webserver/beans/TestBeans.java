package org.webpieces.webserver.beans;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.mock.lib.MockExecutor;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.basic.app.biz.UserDto;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestBeans extends AbstractWebpiecesTest {
	
	
	private MockSomeLib mockSomeLib = new MockSomeLib();
	private MockSomeOtherLib mockSomeOtherLib = new MockSomeOtherLib();
	private MockExecutor mockExecutor = new MockExecutor();
	private Http11Socket http11Socket;

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("beansMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, new AppOverridesModule(), false, metaFile);
		webserver.start();
		http11Socket = http11Simulator.openHttp();
	}

	@Test
    public void testPageParam() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/pageparam");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Hi Dean Hiller, this is testing");
		response.assertContains("Or we can try to get a flash: testflashvalue");
    }

    @Test
    public void testPageParamAsync() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/pageparam_async");
		
		http11Socket.send(req);
		
		Runnable runnable = mockExecutor.getRunnablesScheduled().get(0);
		runnable.run();
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Hi Dean Hiller, this is testing");
		response.assertContains("Or we can try to get a flash: testflashvalue");
    }

	@Test
	public void testPostFailDueToSecureTokenCheck() {
		HttpRequest req = Requests.createPostRequest("/postuser", 
				"user.firstName", "D&D", 
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		//We should change this to a 400 bad request
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}
	
	@Test
	public void testComplexBeanSaved() {
		HttpRequest req = Requests.createPostRequest("/postuser2", 
				"user.firstName", "D&D", 
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserDto user = mockSomeOtherLib.getUser();
		Assert.assertEquals(555, user.getAddress().getZipCode());
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals("Coolness Dr.", user.getAddress().getStreet());
	}

	/**
	 * Found this bug where it blew up translating 'null' to 'null' and tried to convert it
	 *  to int instead, so added test then fixed
	 */
	@Test
	public void testNullIdFromForm() {
		HttpRequest req = Requests.createPostRequest("/postuser2",
				"user.id", "" //multipart is "" and nearly all webservers convert that to null(including ours)
				);
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		//should not have any errors and should redirect back to list of users page..
		Assert.assertEquals("http://myhost.com/listusers", response.getRedirectUrl());
	}
	
	@Test
	public void testInvalidComplexBean() {
		HttpRequest req = Requests.createPostRequest("/postuser2", 
				"user.firstName", "D&D", 
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "This test value invalid since not an int",
				"user.address.street", "Coolness Dr.",
				"password", "should be hidden from flash");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserDto savedUser = mockSomeOtherLib.getUser();
		Assert.assertEquals(null, savedUser); //user was not 

		UserDto user = mockSomeLib.getUser();
		Assert.assertEquals(0, user.getAddress().getZipCode()); //this is not set since it was invalid
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals("Coolness Dr.", user.getAddress().getStreet());
	}

	@Test
	public void testArrayForm() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/arrayForm");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("value=`FirstAccName`".replace('`', '"'));
		response.assertContains("value=`SecondAccName`".replace('`', '"'));
		response.assertContains("value=`someStreet2-0`".replace('`', '"'));
		response.assertContains("value=`someStreet2-1`".replace('`', '"'));

		//default labels are there...
		response.assertContains("First NameX");
		response.assertContains("StreetX");
	}

	@Test
	public void testIncomingDataAndDataSeperate() {
		HttpRequest req = Requests.createPostRequest("/postArray2",
				"user.accounts[1].name", "Account2Name",
				"user.accounts[1].color", "green",
				"user.accounts[2].addresses[0].number", "56",
				"user.firstName", "D&D",
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller"
		);

		DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
		HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
		ByteBuffer buffer = parser.marshalToByteBuffer(req);
		DataWrapper data = dataGen.wrapByteBuffer(buffer);
		
		// Split the body in half
		List<? extends DataWrapper> split = dataGen.split(data, data.getReadableSize() - 20);

		http11Socket.sendBytes(split.get(0));
		http11Socket.sendBytes(split.get(1));
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);

		UserDto user = mockSomeLib.getUser();
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals(3, user.getAccounts().size());
		Assert.assertEquals("Account2Name", user.getAccounts().get(1).getName());
		Assert.assertEquals(56, user.getAccounts().get(2).getAddresses().get(0).getNumber());
	}
	@Test
	public void testArraySaved() {
		HttpRequest req = Requests.createPostRequest("/postArray2", 
				"user.accounts[1].name", "Account2Name",
				"user.accounts[1].color", "green",
				"user.accounts[2].addresses[0].number", "56",
				"user.firstName", "D&D", 
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller"
				);
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserDto user = mockSomeLib.getUser();
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals(3, user.getAccounts().size());
		Assert.assertEquals("Account2Name", user.getAccounts().get(1).getName());
		Assert.assertEquals(56, user.getAccounts().get(2).getAddresses().get(0).getNumber());
	}
	
	/*
	 * Have the controller method be postUser(UserDbo user, String password) BUT then in the html have
	 * entity.name, entity.age, entity.password INSTEAD of user.name, etc. such that there
	 * is a mismatch and verify there is a clean error for that
	 * 
	 * GET /adduser HTTP/1.1
	 * Host: localhost:59786
	 * User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:47.0) Gecko/20100101 Firefox/47.0
	 * Accept: text/html,application/xhtml+xml,application/xml;q=0.9,XX/XX;q=0.8
	 * Accept-Language: en-US,en;q=0.5
	 * Accept-Encoding: gzip, deflate
	 * Referer: http://localhost:59786/adduser
	 * Cookie: webSession=1-gzvc03bKRP2YYvWySwgENREwFSg=:__ST=3a2fda5dad7547d3b15b1f61bd3d12f5; webFlash=1:_message=Invalid+values+below&user.address.zipCode=Text+instead+of+number&__secureToken=3a2fda5dad7547d3b15b1f61bd3d12f5&user.firstName=Dean+Hiller; webErrors=1:user.address.zipCode=Could+not+convert+value
	 * Connection: keep-alive
	 */
	@Test
	public void testDeveloperMistypesBeanNameVsFormNames() {
		HttpRequest req = Requests.createPostRequest("/postuser", 
				"entity.firstName", "D&D", 
				"entity.lastName", "Hiller",
				"entity.fullName", "Dean Hiller",
				"password", "hi"
				);
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}
	
	@Test
	public void testDeveloperMistypesBeanNameVsFormNamesButNullableIsUsed() {
		HttpRequest req = Requests.createPostRequest("/postusernullable", 
				"entity.firstName", "D&D", 
				"entity.lastName", "Hiller",
				"entity.fullName", "Dean Hiller",
				"password", "hi"
				);
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
	
	@Test
	public void testQueryParamsToUserBean() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/getuser");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
	}
	
	@Test
	public void testBeanMissingForGetSoNotFoundResults() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/getuser?user.firstName=jeff&password=as");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockSomeOtherLib);
			binder.bind(SomeLib.class).toInstance(mockSomeLib);
			binder.bind(Executor.class).toInstance(mockExecutor);
		}
	}
}
