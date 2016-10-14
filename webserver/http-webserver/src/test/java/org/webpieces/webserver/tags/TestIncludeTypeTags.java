package org.webpieces.webserver.tags;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.api.TagOverridesModule;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class TestIncludeTypeTags {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		TemplateCompileConfig config = new TemplateCompileConfig();
		Module allOverrides = Modules.combine(new PlatformOverridesForTest(config), new TagOverridesModule(TagOverrideLookupForTesting.class));
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(allOverrides, null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testCustomTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/customtag");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Page Using Custom Tag");
		response.assertContains("This is a custom tag which can also use tags in itself <a href=`/verbatim`>Some Link Here</a>".replace('`', '"'));
		response.assertContains("The user is override and Dean Hiller tag argument cool"); //using variable in custom tag
		response.assertContains("After Custom Tag");
		response.assertContains("supertemplate BEGIN");
		response.assertContains("supertemplate END");
	}
	
	@Test
	public void testRenderTagArgsTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/renderTagArgs");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Page Using renderTagArgs Tag");
		response.assertContains("The user is override"); //using variable from tag args in the called template
		response.assertContains("After renderTagArgs Tag");
	}
	
	@Test
	public void testRenderPageArgsTag() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/renderPageArgs");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("Page Using renderPageArgs Tag");
		response.assertContains("The user is Dean Hiller"); //using variable from page args in the called template
		response.assertContains("After renderPageArgs Tag");
	}
}
