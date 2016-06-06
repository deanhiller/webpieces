package org.webpieces.router.api.error.dev;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.mocks.VirtualFileInputStream;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

public class ErrorTest {
	
	private static final Logger log = LoggerFactory.getLogger(ErrorTest.class);

	private CompileConfig compileConfig;

	@Before
	public void setUp() {
		String filePath = System.getProperty("user.dir");
		File myCodePath = new File(filePath + "/src/test/java");
		compileConfig = new CompileConfig(new VirtualFileImpl(myCodePath));		
	}
	
	@Test
	public void testNoMethod() {
		log.info("starting");

		String moduleFileContents = NoMethodRouterModules.class.getName();
		VirtualFile f = new VirtualFileInputStream(moduleFileContents.getBytes(), "testAppModules");		
		RoutingService server = DevRouterFactory.create(f, compileConfig);
		Request req = createHttpRequest(HttpMethod.GET, "/something");

		//this should definitely not throw since we lazy load everything in dev...
		server.start();
		
		try {
			server.processHttpRequests(req);
			Assert.fail("should have thrown");
		} catch(IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("Cannot find 'public' method="));
		}
		
	}

	private Request createHttpRequest(HttpMethod method, String path) {
		Request r = new Request();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}

}
