package org.webpieces.webserver.i18n;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class TestI18n extends AbstractWebpiecesTest {
	
	
	private HttpSocket http11Socket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("i18nMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false), null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}

	@Test
	public void testDefaultText() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/i18nBasic");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("This is the default text that would go here and can be quite a long\n      version");
		response.assertContains("Hi Dean, we would like to take you to Italy");
	}

	@Test
	public void testChineseText() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/i18nBasic");
		req.addHeader(new Header(KnownHeaderName.ACCEPT_LANGUAGE, "zh-CN"));
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains("你好， 这个是一个比较长的一个东西 我可以写比较多。  我在北京师范大学学了中文。 我喜欢完冰球");
		response.assertContains("你好Dean，我们要去Italy");
	}
}
