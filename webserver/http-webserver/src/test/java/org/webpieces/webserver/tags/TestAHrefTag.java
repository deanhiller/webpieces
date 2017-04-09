package org.webpieces.webserver.tags;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockResponseSender;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestAHrefTag {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testAHref() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/ahref");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("<a href=`/verbatim` id=`myid`>My link</a>".replace('`', '"'));
		response.assertContains("<a href=`/verbatim` id=`myid`>My link2</a>".replace('`', '"'));
		response.assertContains("<a href=`/if`>My render link</a>".replace('`', '"'));
		response.assertContains("<a href=`/if`>My render link2</a>".replace('`', '"'));
		response.assertContains("<a href=`/redirect/Dean+Hiller`>The link</a>".replace('`', '"'));
		response.assertContains("<a href=`/redirect/Dean+Hiller`>PureALink</a>".replace('`', '"'));
		response.assertContains("Link but no ahref='/redirect/Dean+Hiller'");
	}
	

}
