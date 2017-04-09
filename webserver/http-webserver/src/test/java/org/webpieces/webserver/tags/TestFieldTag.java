package org.webpieces.webserver.tags;

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

public class TestFieldTag {

	private MockResponseSender socket = new MockResponseSender();
	private RequestListener server;
	
	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("tagsMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new PlatformOverridesForTest(), null, false, metaFile);
		server = webserver.start();
	}

	@Test
	public void testBasic() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/customFieldTag");
		
		server.incomingRequest(req, new RequestId(0), true, socket);
		
        FullResponse response = ResponseExtract.assertSingleResponse(socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		//This can be a bit brittle if people change field.tag but we HAVE to verify this html was not escaped on accident as it was previously
		response.assertContains("<div class=`controls`>".replace('`', '"'));
		response.assertContains("<input type=`text` name=`user` value=`Dean` class=`input-xlarge`/>".replace('`', '"'));
		response.assertContains("<span id=`user_errorMsg` class=`help-block`></span>".replace('`', '"'));
	}

}
