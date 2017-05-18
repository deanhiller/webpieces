package org.webpieces.webserver.i18n;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.ResponseExtract;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.Http11Socket;

public class TestI18n extends AbstractWebpiecesTest {
	
	
	private Http11Socket http11Socket;

	@Before
	public void setUp() {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("i18nMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = http11Simulator.openHttp();
	}

	@Test
	public void testDefaultText() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/i18nBasic");
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the default text that would go here and can be quite a long\n      version");
		response.assertContains("Hi Dean, we would like to take you to Italy");
	}

	@Test
	public void testChineseText() {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/i18nBasic");
		req.addHeader(new Header(KnownHeaderName.ACCEPT_LANGUAGE, "zh-CN"));
		
		http11Socket.send(req);
		
		FullResponse response = ResponseExtract.assertSingleResponse(http11Socket);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("你好， 这个是一个比较长的一个东西 我可以写比较多。  我在北京师范大学学了中文。 我喜欢完冰球");
		response.assertContains("你好Dean，我们要去Italy");
	}
}
