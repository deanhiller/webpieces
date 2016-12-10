package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.router.api.ProdRouterModule;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class TestCompressionCache {

	private static final Logger log = LoggerFactory.getLogger(TestCompressionCache.class);
	
	private CompressionCacheSetup cache;
	private TestFileUtilProxy proxy = new TestFileUtilProxy();

	@Before
	public void setUp() throws IOException {
		File cacheDir = new File(System.getProperty("java.io.tmpdir")+"/cacheForTesting");
		FileUtils.deleteDirectory(cacheDir);
		log.info("deleting dir="+cacheDir);
		File stagingDir = new File("output/staging");
		FileUtils.deleteDirectory(stagingDir);
		
		RouterConfig config = new RouterConfig();
		config.setSecretKey(SecretKeyInfo.generateForTest());
		config.setCachedCompressedDirectory(cacheDir);
		Module allMods = Modules.override(new ProdRouterModule(config)).with(new TestModule());
		Injector injector = Guice.createInjector(allMods);
		cache = injector.getInstance(CompressionCacheSetup.class);
	}
	
	@Test
	public void testStartServerTwiceNoChanges() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = new File("output/staging");
		FileUtils.copyDirectory(f, stagingDir);
		
		List<StaticRoute> routes = runBasicServerOnce(stagingDir);
		
		//if server is just restarted(no file changes), we should skip reading files...
		cache.setupCache(routes);
		
		Assert.assertEquals(0, proxy.getReadFiles().size());
		Assert.assertEquals(0, proxy.getCompressedFiles().size());		
	}

	private List<StaticRoute> runBasicServerOnce(File stagingDir) {
		List<StaticRoute> routes = new ArrayList<>();
		routes.add(new StaticRoute(0, "/public/", stagingDir.getAbsolutePath()+"/", false));
		cache.setupCache(routes);
		Assert.assertEquals(2, proxy.getReadFiles().size());
		Assert.assertEquals(2, proxy.getCompressedFiles().size());
		proxy.clear();
		return routes;
	}
	
	@Test
	public void testStartServerTwiceButUrlPathChanges() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = new File("output/staging");
		FileUtils.copyDirectory(f, stagingDir);
		
		runBasicServerOnce(stagingDir);
		
		List<StaticRoute> routes2 = new ArrayList<>();
		routes2.add(new StaticRoute(0, "/public1.4/", stagingDir.getAbsolutePath()+"/", false));

		//if server is just restarted(no file changes), we should skip reading files...
		cache.setupCache(routes2);
		
		Assert.assertEquals(2, proxy.getReadFiles().size());
		Assert.assertEquals(2, proxy.getCompressedFiles().size());		
	}
	
	@Test
	public void testCreateCacheAndUpdateTimestampButNotChangeFileContents() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = new File("output/staging");
		FileUtils.copyDirectory(f, stagingDir);
		
		List<StaticRoute> routes = runBasicServerOnce(stagingDir);
		
		FileUtils.copyDirectory(f, stagingDir, false); //do not preserve dates here...
		
		//if server is just restarted(no file changes), we should skip reading files...
		cache.setupCache(routes);
		
		Assert.assertEquals(2, proxy.getReadFiles().size());
		Assert.assertEquals(0, proxy.getCompressedFiles().size());		
	}
	
	@Test
	public void testModifyFileContentsButNotUrlPathFailure() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = new File("output/staging");
		FileUtils.copyDirectory(f, stagingDir);
		
		List<StaticRoute> routes = runBasicServerOnce(stagingDir);

		File f2 = new File("src/test/resources/cacheTest2");
		FileUtils.deleteDirectory(stagingDir);
		FileUtils.copyDirectory(f2, stagingDir, false); //do not preserve dates here...

		try {
			cache.setupCache(routes);
			Assert.fail("should have failed since file contents changed and url path didn't which would have broken web app customers");
		} catch(IllegalStateException e) {
		}
	}
	
	private class TestModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(FileUtil.class).toInstance(proxy);
		}
	}
}
