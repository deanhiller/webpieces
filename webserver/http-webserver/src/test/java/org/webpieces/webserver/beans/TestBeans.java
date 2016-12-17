package org.webpieces.webserver.beans;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.basic.app.biz.UserDto;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestBeans {

	private RequestListener server;
	private MockResponseSender mockResponseSender = new MockResponseSender();
	private MockSomeLib mockSomeLib = new MockSomeLib();
	private MockSomeOtherLib mockSomeOtherLib = new MockSomeOtherLib();
	

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("beansMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), new AppOverridesModule(), false, metaFile);
		server = webserver.start();
	}

	@Test
    public void testPageParam() {
        pageParamAux(false);
    }

    @Test
    public void testPageParamAsync() {
        pageParamAux(true);
    }

	private void pageParamAux(Boolean async) {
        String uri;
        if(async) {
            uri = "/pageparam_async";
        } else {
            uri = "/pageparam";
        }

        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, uri);

		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);

        // In case we are async, wait up to 500ms
		List<FullResponse> responses = mockResponseSender.getResponses(500, 1);
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Hi Dean Hiller, this is testing");
		response.assertContains("Or we can try to get a flash: testflashvalue");
	}


	@Test
    public void testFlashMessage() {
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/flashmessage");
        server.incomingRequest(req, new RequestId(0), true, mockResponseSender);

        List<FullResponse> responses = mockResponseSender.getResponses();
        Assert.assertEquals(1, responses.size());

        FullResponse response = responses.get(0);
        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("Msg: it worked");
    }

    @Test
    public void testValidationError() {
        HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/validationerror");
        server.incomingRequest(req, new RequestId(0), true, mockResponseSender);

        List<FullResponse> responses = mockResponseSender.getResponses();
        Assert.assertEquals(1, responses.size());

        FullResponse response = responses.get(0);
        response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
        response.assertContains("Err: it failed");
    }

	@Test
	public void testPostFailDueToSecureTokenCheck() {
		HttpRequest req = Requests.createPostRequest("/postuser", 
				"user.firstName", "D&D", 
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller",
				"user.address.zipCode", "555",
				"user.address.street", "Coolness Dr.");
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
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
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserDto user = mockSomeOtherLib.getUser();
		Assert.assertEquals(555, user.getAddress().getZipCode());
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals("Coolness Dr.", user.getAddress().getStreet());
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
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
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
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
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
	public void testIncomingRequestAndDataSeperate() {
		HttpRequest req = Requests.createPostRequest("/postArray2",
				"user.accounts[1].name", "Account2Name",
				"user.accounts[1].color", "green",
				"user.accounts[2].addresses[0].number", "56",
				"user.firstName", "D&D",
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller"
		);

		DataWrapper data = req.getBody();
		DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

		// Split the body in half
		List<? extends DataWrapper> split = dataGen.split(data, data.getReadableSize() / 2);
		req.setBody(dataGen.emptyWrapper());
		RequestId id = new RequestId(0);

		server.incomingRequest(req, id, false, mockResponseSender);
		server.incomingData(split.get(0), id, false, mockResponseSender);
		server.incomingData(split.get(1), id, true, mockResponseSender);

		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
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
		
		server.incomingRequest(req, new RequestId(0), true, mockResponseSender);
		
		List<FullResponse> responses = mockResponseSender.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		
		UserDto user = mockSomeLib.getUser();
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals(3, user.getAccounts().size());
		Assert.assertEquals("Account2Name", user.getAccounts().get(1).getName());
		Assert.assertEquals(56, user.getAccounts().get(2).getAddresses().get(0).getNumber());
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockSomeOtherLib);
			binder.bind(SomeLib.class).toInstance(mockSomeLib);
		}
	}
}
