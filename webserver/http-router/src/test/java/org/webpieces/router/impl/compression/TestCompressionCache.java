package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.router.api.EmptyPortConfigLookup;
import org.webpieces.router.api.PortConfigLookup;
import org.webpieces.router.api.ProdRouterModule;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.routers.EStaticRouter;
import org.webpieces.router.impl.routers.MatchInfo;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileFactory;
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
	private File cacheDir = FileFactory.newTmpFile("webpieces/cacheForTesting"); 

	@Before
	public void setUp() throws IOException {
		FileUtils.deleteDirectory(cacheDir);
		log.info("deleting dir="+cacheDir);
		File stagingDir = FileFactory.newBaseFile("output/staging");
		FileUtils.deleteDirectory(stagingDir);
		
		RouterConfig config = new RouterConfig(FileFactory.getBaseWorkingDir()).setPortLookupConfig(new EmptyPortConfigLookup());
		config.setSecretKey(SecretKeyInfo.generateForTest());
		config.setCachedCompressedDirectory(cacheDir);
		Module allMods = Modules.override(new ProdRouterModule(config)).with(new TestModule());
		Injector injector = Guice.createInjector(allMods);
		cache = injector.getInstance(CompressionCacheSetup.class);
	}
	
	@Test
	public void testStartServerTwiceNoChanges() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = FileFactory.newBaseFile("output/staging");
		FileUtils.copyDirectory(f, stagingDir);
		
		List<EStaticRouter> routes = runBasicServerOnce(stagingDir);
		
		//if server is just restarted(no file changes), we should skip reading files...
		cache.setupCache(routes);
		
		Assert.assertEquals(0, proxy.getReadFiles().size());
		Assert.assertEquals(0, proxy.getCompressedFiles().size());		
	}

	private EStaticRouter create(Port port, UrlPath urlPath, VirtualFile fileSystemPath, boolean isOnClassPath, File targetCatchLocation) {
		MatchInfo info = new MatchInfo(urlPath, port, null, null, null, null);
		return new EStaticRouter(null, info, fileSystemPath, isOnClassPath, targetCatchLocation, false);
	}
	private List<EStaticRouter> runBasicServerOnce(File stagingDir) {
		List<EStaticRouter> routes = new ArrayList<>();
		VirtualFile dir = VirtualFileFactory.newFile(stagingDir);
		
		routes.add(create(Port.BOTH, new UrlPath("", "/public/"), dir, false, cacheDir));
		cache.setupCache(routes);
		Assert.assertEquals(2, proxy.getReadFiles().size());
		Assert.assertEquals(2, proxy.getCompressedFiles().size());
		proxy.clear();
		return routes;
	}
	
	@Test
	public void testStartServerTwiceButUrlPathChanges() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = FileFactory.newBaseFile("output/staging");

		FileUtils.copyDirectory(f, stagingDir);
		
		runBasicServerOnce(stagingDir);
		
		List<EStaticRouter> routes2 = new ArrayList<>();
		VirtualFile dir = VirtualFileFactory.newFile(stagingDir);
		routes2.add(create(Port.BOTH, new UrlPath("", "/public1.4/"), dir, false, cacheDir));

		//if server is just restarted(no file changes), we should skip reading files...
		cache.setupCache(routes2);
		
		Assert.assertEquals(2, proxy.getReadFiles().size());
		Assert.assertEquals(2, proxy.getCompressedFiles().size());		
	}
	
	@Test
	public void testCreateCacheAndUpdateTimestampButNotChangeFileContents() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = FileFactory.newBaseFile("output/staging");

		FileUtils.copyDirectory(f, stagingDir);
		
		List<EStaticRouter> routes = runBasicServerOnce(stagingDir);
		
		FileUtils.copyDirectory(f, stagingDir, false); //do not preserve dates here...
		
		//if server is just restarted(no file changes), we should skip reading files...
		cache.setupCache(routes);
		
		Assert.assertEquals(2, proxy.getReadFiles().size());
		Assert.assertEquals(0, proxy.getCompressedFiles().size());		
	}
	
	@Test
	public void testModifyFileContentsButNotUrlPathFailure() throws IOException {
		File f = new File("src/test/resources/cacheTest1");
		File stagingDir = FileFactory.newBaseFile("output/staging");

		FileUtils.copyDirectory(f, stagingDir);
		
		List<EStaticRouter> routes = runBasicServerOnce(stagingDir);

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
			
			binder.bind(PortConfigLookup.class).toInstance(new EmptyPortConfigLookup());
		}
	}
}
