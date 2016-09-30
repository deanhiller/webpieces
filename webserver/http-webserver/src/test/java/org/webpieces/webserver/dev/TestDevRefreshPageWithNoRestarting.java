package org.webpieces.webserver.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.Requests;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.MockFrontendSocket;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class TestDevRefreshPageWithNoRestarting {

	private static final Logger log = LoggerFactory.getLogger(TestDevSynchronousErrors.class);
	private HttpRequestListener server;
	private MockFrontendSocket socket = new MockFrontendSocket();
	private File stashedExistingCodeDir;
	private File existingCodeLoc;
	private String userDir;
	
	@Before
	public void setUp() throws ClassNotFoundException, IOException {
		Asserts.assertWasCompiledWithParamNames("test");
		userDir = System.getProperty("user.dir");
		log.info("running from dir="+userDir);

		existingCodeLoc = new File(userDir+"/src/test/java/org/webpieces/webserver/dev/app");
		
		//developers tend to exit their test leaving the code in a bad state so if they run it again, restore the original
		//version for them(if we change the original version, we have to copy it to this directory as well though :(
		File original = new File(userDir+"/src/test/devServerTest/devServerOriginal");
		FileUtils.copyDirectory(original, existingCodeLoc, null);
		
		//cache existing code for use by teardown...

		stashedExistingCodeDir = new File(System.getProperty("java.io.tmpdir")+"/webpiecesTestDevServer/app");
		FileUtils.copyDirectory(existingCodeLoc, stashedExistingCodeDir);
		
		//list all source paths here as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(userDir+"/src/test/java"));
		
		VirtualFile metaFile = new VirtualFileImpl(userDir + "/src/test/resources/devMeta.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths);
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths);
		
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new DevTemplateModule(templateConfig),
										new MockFrontEndModule());
		
		WebserverForTest webserver = new WebserverForTest(platformOverrides, null, false, metaFile);
		server = webserver.start();
	}
	
	@After
	public void tearDown() throws IOException {
		//delete any modifications and restore the original code...
		FileUtils.deleteDirectory(existingCodeLoc);
		FileUtils.copyDirectory(stashedExistingCodeDir, existingCodeLoc);
	}
	
	@Test
	public void testGuiceModuleAddAndControllerChange() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		server.processHttpRequests(socket, req , false);
		verifyPageContents("user=Dean Hiller");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/guiceModule");
		
		server.processHttpRequests(socket, req, false);
		verifyPageContents("newuser=Joseph");
	}

	//Different than swapping out meta 
	@Test
	public void testJustControllerChanged() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home");
		server.processHttpRequests(socket, req , false);
		verifyPageContents("user=Dean Hiller");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/controllerChange");
		
		server.processHttpRequests(socket, req, false);
		verifyPageContents("user=CoolJeff");
	}

	@Test
	public void testRouteAdditionWithNewControllerPath() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/newroute");
		server.processHttpRequests(socket, req , false);
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		socket.clear();
		
		simulateDeveloperMakesChanges("src/test/devServerTest/routeChange");
		
		server.processHttpRequests(socket, req, false);
		verifyPageContents("Existing Route Page");
	}
	
	@Test
	public void testFilterChanged() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/filter");
		server.processHttpRequests(socket, req , false);
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/home", response.getRedirectUrl());
		socket.clear();
		
		simulateDeveloperMakesChanges("src/test/devServerTest/filterChange");
		
		server.processHttpRequests(socket, req, false);
		
		responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/causeError", response.getRedirectUrl());
	}

	@Test
	public void testNotFoundFilterNotChangedAndTwoRequests() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/notFound?webpiecesShowPage");
		server.processHttpRequests(socket, req , false);
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/home", response.getRedirectUrl());
		socket.clear();
		
		server.processHttpRequests(socket, req, false);
		
		responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);
		Assert.assertEquals("http://myhost.com/home", response.getRedirectUrl());
	}
	
	@Test
	public void testNotFoundRouteModifiedAndControllerModified() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/notfound/notfound?webpiecesShowPage=true");
		server.processHttpRequests(socket, req , false);
		verify404PageContents("value1=something1");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/notFound");
		
		server.processHttpRequests(socket, req, false);
		verify404PageContents("value2=something2");
	}
	
	@Test
	public void testInternalErrorModifiedAndControllerModified() throws IOException {
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/causeError");
		server.processHttpRequests(socket, req , false);
		verify500PageContents("InternalError1=error1");
		
		simulateDeveloperMakesChanges("src/test/devServerTest/internalError");
		
		server.processHttpRequests(socket, req, false);
		verify500PageContents("InternalError2=error2");		
	}
	
	private void simulateDeveloperMakesChanges(String directory) throws IOException {
		File srcDir = new File(userDir+"/"+directory);
		FileUtils.copyDirectory(srcDir, existingCodeLoc, null, false);
	}

	private void verifyPageContents(String contents) {
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_200_OK);
		response.assertContains(contents);
		socket.clear();
	}
	
	private void verify404PageContents(String contents) {
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains(contents);
		socket.clear();
	}
	
	private void verify500PageContents(String contents) {
		List<FullResponse> responses = socket.getResponses();
		Assert.assertEquals(1, responses.size());

		FullResponse response = responses.get(0);
		response.assertStatusCode(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR);
		response.assertContains(contents);
		socket.clear();
	}
}
