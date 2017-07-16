package org.webpieces.webserver;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.SecretKeyInfo;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.inject.Module;

public class WebserverForTest {
	
	private static final Logger log = LoggerFactory.getLogger(WebserverForTest.class);
	private File cacheDir =  new File(System.getProperty("java.io.tmpdir")+"/webpiecesCache/webserverForTest");
	public static final Charset CHAR_SET_TO_USE = StandardCharsets.UTF_8;

	public static void main(String[] args) throws InterruptedException {
		new WebserverForTest(null, null, false, null).start();
		
		synchronized (WebserverForTest.class) {
			//wait forever for now so server doesn't shut down..
			WebserverForTest.class.wait();
		}	
	}

	private WebServer webServer;

	public WebserverForTest(Module platformOverrides, Module appOverrides, boolean usePortZero, VirtualFile metaFile) {
		this(new TestConfig(platformOverrides, appOverrides, usePortZero, metaFile, true));
	}
	
	public WebserverForTest(TestConfig testConfig) {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		VirtualFile metaFile = testConfig.getMetaFile();
		//Tests can override this...
		if(testConfig.getMetaFile() == null)
			metaFile = new VirtualFileClasspath("basicMeta.txt", WebserverForTest.class.getClassLoader());

		int httpPort = 8080;
		int httpsPort = 8443;
		if(testConfig.isUsePortZero()) {
			httpPort = 0;
			httpsPort = 0;
		}
		
		File baseWorkingDir = FileFactory.getBaseWorkingDir();

		//3 pieces to the webserver so a configuration for each piece
		WebServerConfig config = new WebServerConfig()
				.setPlatformOverrides(testConfig.getPlatformOverrides())
				.setHttpListenAddress(new InetSocketAddress(httpPort))
				.setHttpsListenAddress(new InetSocketAddress(httpsPort))
				.setSslEngineFactory(new SSLEngineFactoryWebServerTesting())
				.setFunctionToConfigureServerSocket(s -> configure(s));
		RouterConfig routerConfig = new RouterConfig(baseWorkingDir)
											.setMetaFile(metaFile )
											.setWebappOverrides(testConfig.getAppOverrides())
											.setFileEncoding(CHAR_SET_TO_USE)
											.setDefaultResponseBodyEncoding(CHAR_SET_TO_USE)
											.setCachedCompressedDirectory(cacheDir)
											.setSecretKey(SecretKeyInfo.generateForTest())
											.setTokenCheckOn(testConfig.isUseTokenCheck())
											.setPortConfigCallback(() -> fetchPortsForRedirects());
		TemplateConfig templateConfig = new TemplateConfig();
		
		webServer = WebServerFactory.create(config, routerConfig, templateConfig);
	}

	PortConfig fetchPortsForRedirects() {
		int httpPort = getUnderlyingHttpChannel().getLocalAddress().getPort();
		int httpsPort = getUnderlyingHttpsChannel().getLocalAddress().getPort();
		return new PortConfig(httpPort, httpsPort);
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
