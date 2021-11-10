package org.webpieces.webserver.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileFactory;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.OverridesForEmbeddedSvrWithParsing;
import org.webpieces.webserver.test.ResponseExtract;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestDevRefreshPageWithNoRestarting extends AbstractWebpiecesTest {

	private static final Logger log = LoggerFactory.getLogger(TestDevSynchronousErrors.class);
	
	private File stashedExistingCodeDir;
	private File existingCodeLoc;
	private String userDir;

	private HttpSocket http11Socket;
	
	@Before
	public void setUp() throws ClassNotFoundException, IOException, InterruptedException, ExecutionException, TimeoutException {
		Asserts.assertWasCompiledWithParamNames("test");
		userDir = System.getProperty("user.dir");
		log.info("running from dir="+userDir);

		existingCodeLoc = FileFactory.newBaseFile("src/test/java/org/webpieces/webserver/dev/app");
		
		//developers tend to exit their test leaving the code in a bad state so if they run it again, restore the original
		//version for them(if we change the original version, we have to copy it to this directory as well though :(
		File original = FileFactory.newBaseFile("src/test/devServerTest/devServerOriginal");
		FileUtils.copyDirectory(original, existingCodeLoc, null);
		
		//cache existing code for use by teardown...

		stashedExistingCodeDir = FileFactory.newCacheLocation("webpieces/"+getClass().getSimpleName()+"/javaFiles");
		FileUtils.copyDirectory(existingCodeLoc, stashedExistingCodeDir);
		
		//list all source paths here as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(VirtualFileFactory.newBaseFile("src/test/java"));
		
		VirtualFile metaFile = VirtualFileFactory.newBaseFile("src/test/resources/devMeta.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths);
		VirtualFile cacheLocation = new VirtualFileImpl(FileFactory.newCacheLocation("webpieces/"+getClass().getSimpleName()+"/bytecode"));
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths, cacheLocation);
		
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new OverridesForEmbeddedSvrWithParsing(mgr, time, mockTimer, templateConfig, metrics));
		
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(platformOverrides, null, false, metaFile);
		webserver.start();
		http11Socket = connectHttp(false, webserver.getUnderlyingHttpChannel().getLocalAddress());
	}
	
	@After
	public void tearDown() throws IOException {
		//delete any modifications and restore the original code...
		FileUtils.deleteDirectory(existingCodeLoc);
		FileUtils.copyDirectory(stashedExistingCodeDir, existingCodeLoc);
	}
	
	@Test
	public void testGuiceModuleAddAndControllerChange() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verifyPageContents(respFuture1, "user=Dean Hiller");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/guiceModule");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		verifyPageContents(respFuture, "newuser=Joseph");
	}

	//Different than swapping out meta 
	@Test
	public void testJustControllerChanged() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verifyPageContents(respFuture1, "user=Dean Hiller");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/controllerChange");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		verifyPageContents(respFuture, "user=CoolJeff");
	}

	@Test
	public void testJustControllerChangedToHaveBug() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verifyPageContents(respFuture1, "user=Dean Hiller");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/controllerChangeThrowsError");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		
		response.assertContains("You are in the WebPieces Development Server. You appear to have a bug in your application.");

		//response contains exception message
		response.assertContains("Simulated bug");
		
		
		//Encountered a bug and we can show what would have been shown in production...
		response.assertContains("Your app in production will look like what is in this iframe");
		response.assertContains("/home?webpiecesShowInternalErrorPage=true");
	}
	
	@Test
	public void testJustControllerChangedToCompileError() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verifyPageContents(respFuture1, "user=Dean Hiller");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/controllerChangeCompileError");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		
		response.assertContains("You are in the WebPieces Development Server. You appear to have a bug in your application.");
		
		//Encountered a bug that can't happen in production as prod server won't even start...
		response.assertContains("The above looks like an issue that cannot happen in production so we can't display a production page here");
		
		//showing the compile error
		response.assertContains("<span style=\"color:red;\">Error: cannot find symbol\n" +
				"  symbol:   method renThis()\n" +
				"  location: class org.webpieces.router.api.controller.actions.Actions</span>");
	}
	
	@Test
	public void testRouteAdditionWithNewControllerPath() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/newroute");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture1);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		
		simulateDeveloperMakesChanges("src/test/devServerTest/routeChange");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		verifyPageContents(respFuture, "Existing Route Page");
	}
	
	@Test
	public void testFilterChanged() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/filter");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture1);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/home", response.getRedirectUrl());
		
		simulateDeveloperMakesChanges("src/test/devServerTest/filterChange");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/causeError", response.getRedirectUrl());
	}

	@Test
	public void testNotFoundDisplaysWithIframeANDSpecialUrl() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/notFound");
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);

		//platform should convert request into a development not found page which has an iframe
		//of the original page with a query param to tell platform to display original 
		//page requested 
		response.assertContains("<iframe id=\"production\" src=\"/notFound?webpiecesShowPage=true");
	}
	
	@Test
	public void testNotFoundFilterNotChangedAndTwoRequests() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/anyNotFound?webpiecesShowPage");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verify404PageContents(respFuture1, "value1=something1");

		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		verify404PageContents(respFuture, "value1=something1");
	}
	
	@Test
	public void testNotFoundRouteModifiedAndControllerModified() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/anyNotfound?webpiecesShowPage=true");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verify404PageContents(respFuture1, "value1=something1");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/notFound");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		verify404PageContents(respFuture, "value2=something2");
	}

	@Test
	public void testNotFoundRouteModifiedAndControllerModifiedAndInvalidRoute() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/anyNotfound?webpiecesShowPage=true");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);
		verify404PageContents(respFuture1, "value1=something1");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/notFoundNewInvalidRoute");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		
		response.assertContains("You are in the WebPieces Development Server. You appear to have a bug in your application.");
		
		//Encountered a bug that can't happen in production as prod server won't even start...
		response.assertContains("The above looks like an issue that cannot happen in production so we can't display a production page here");
	}
	
	@Test
	public void testNotFoundFilterModified() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/enableFilter?webpiecesShowPage=true");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);

		verify303(respFuture1, "http://myhost.com/home");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/notFound");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		verify303(respFuture, "http://myhost.com/filter");
	}

	@Test
	public void testNotFoundFilterModifiedAndInvalidRoute() throws IOException {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/enableFilter?webpiecesShowPage=true");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req);

		verify303(respFuture1, "http://myhost.com/home");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/notFoundNewInvalidRoute");
		
		XFuture<HttpFullResponse> respFuture = http11Socket.send(req);

		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		
		response.assertContains("You are in the WebPieces Development Server. You appear to have a bug in your application.");
		
		//Encountered a bug that can't happen in production as prod server won't even start...
		response.assertContains("The above looks like an issue that cannot happen in production so we can't display a production page here");
	}

	private void verify303(XFuture<HttpFullResponse> respFuture, String url) {
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals(url, response.getRedirectUrl());
	}
	
	@Test
	public void testInternalErrorModifiedAndControllerModified() throws IOException {
		HttpFullRequest req1 = Requests.createRequest(KnownHttpMethod.GET, "/causeError");
		XFuture<HttpFullResponse> respFuture1 = http11Socket.send(req1);
		
		ResponseWrapper response1 = ResponseExtract.waitResponseAndWrap(respFuture1);
		response1.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response1.assertContains("/causeError?webpiecesShowInternalErrorPage=true"); //There should be a callback url to render what shows in production
		
		HttpFullRequest req2 = Requests.createRequest(KnownHttpMethod.GET, "/causeError?webpiecesShowInternalErrorPage=true");
		XFuture<HttpFullResponse> respFuture2 = http11Socket.send(req2);
		ResponseWrapper response2 = ResponseExtract.waitResponseAndWrap(respFuture2);
		response2.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response2.assertContains("InternalError1=error1");
				
		simulateDeveloperMakesChanges("src/test/devServerTest/internalError");

		XFuture<HttpFullResponse> respFuture3 = http11Socket.send(req1);
		ResponseWrapper response3 = ResponseExtract.waitResponseAndWrap(respFuture3);
		response3.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response3.assertContains("/causeError?webpiecesShowInternalErrorPage=true"); //There should be a callback url to render what shows in production		

		XFuture<HttpFullResponse> respFuture4 = http11Socket.send(req2);
		ResponseWrapper response4 = ResponseExtract.waitResponseAndWrap(respFuture4);
		response4.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response4.assertContains("InternalError2=error2");
	}
	
	private void simulateDeveloperMakesChanges(String directory) throws IOException {
		File srcDir = new File(userDir+"/"+directory);
		FileUtils.copyDirectory(srcDir, existingCodeLoc, null, false);
	}

	private void verifyPageContents(XFuture<HttpFullResponse> respFuture, String contents) {
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains(contents);
	}
	
	private void verify404PageContents(XFuture<HttpFullResponse> respFuture, String contents) {
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains(contents);
	}
	
}
