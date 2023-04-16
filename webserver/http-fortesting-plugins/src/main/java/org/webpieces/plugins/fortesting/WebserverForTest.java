package org.webpieces.plugins.fortesting;

import java.io.File;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.security.SecretKeyInfo;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.inject.Module;

public class WebserverForTest {
	
	private static final Logger log = LoggerFactory.getLogger(WebserverForTest.class);
	private File cacheDir = FileFactory.newCacheLocation("webpieces/WebserverForTest/compression");  
	public static final Charset CHAR_SET_TO_USE = StandardCharsets.UTF_8;

	public static void main(String[] args) throws InterruptedException {
		new WebserverForTest(null, null, null, args).start();
		
		synchronized (WebserverForTest.class) {
			//wait forever for now so server doesn't shut down..
			WebserverForTest.class.wait();
		}	
	}

	private WebServer webServer;

	public WebserverForTest(Module platformOverrides, Module appOverrides, VirtualFile metaFile, String ... args) {
		this(new TestConfig(platformOverrides, appOverrides, metaFile, true), args);
	}
	
	public WebserverForTest(TestConfig testConfig, String ...args) {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		VirtualFile metaFile = testConfig.getMetaFile();
		//Tests can override this...
		if(testConfig.getMetaFile() == null)
			metaFile = new VirtualFileClasspath("basicMeta.txt", WebserverForTest.class.getClassLoader());

		Module platformOverrides = testConfig.getPlatformOverrides();
		
		File baseWorkingDir = FileFactory.getBaseWorkingDir();

		//3 pieces to the webserver so a configuration for each piece
		WebServerConfig config = new WebServerConfig()
				.setPlatformOverrides(platformOverrides);
		RouterConfig routerConfig = new RouterConfig(baseWorkingDir, "pluginTests")
											.setMetaFile(metaFile )
											.setWebappOverrides(testConfig.getAppOverrides())
											.setFileEncoding(CHAR_SET_TO_USE)
											.setDefaultResponseBodyEncoding(CHAR_SET_TO_USE)
											.setCachedCompressedDirectory(cacheDir)
											.setSecretKey(SecretKeyInfo.generateForTest())
											.setTokenCheckOn(testConfig.isUseTokenCheck());
		TemplateConfig templateConfig = new TemplateConfig();
		
		webServer = WebServerFactory.create(config, routerConfig, templateConfig, args);
	}
	
	public void configure(ServerSocketChannel channel) throws SocketException {
		channel.socket().setReuseAddress(true);
		//channel.socket().setSoTimeout(timeout);
		//channel.socket().setReceiveBufferSize(size);
	}
	
	public void start() {
		webServer.startSync();
	}

	public void stop() {
		webServer.stop();
	}

	public TCPServerChannel getUnderlyingHttpChannel() {
		return webServer.getUnderlyingHttpChannel();
	}

	public TCPServerChannel getUnderlyingHttpsChannel() {
		return webServer.getUnderlyingHttpsChannel();
	}
	
	public File getCacheDir() {
		return cacheDir;
	}

}
