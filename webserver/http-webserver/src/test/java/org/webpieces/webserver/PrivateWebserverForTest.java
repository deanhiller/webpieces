package org.webpieces.webserver;

import java.io.File;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.SecretKeyInfo;
import org.webpieces.webserver.api.HttpSvrInstanceConfig;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.inject.Module;

public class PrivateWebserverForTest {
	
	private static final Logger log = LoggerFactory.getLogger(PrivateWebserverForTest.class);
	private File cacheDir =  new File(System.getProperty("java.io.tmpdir")+"/webpiecesCache/webserverForTest");
	public static final Charset CHAR_SET_TO_USE = StandardCharsets.UTF_8;

	public static void main(String[] args) throws InterruptedException {
		new PrivateWebserverForTest(null, null, false, null).start();
		
		synchronized (PrivateWebserverForTest.class) {
			//wait forever for now so server doesn't shut down..
			PrivateWebserverForTest.class.wait();
		}	
	}

	private WebServer webServer;

	@Deprecated
	public PrivateWebserverForTest(Module platformOverrides, Module appOverrides, boolean usePortZero, VirtualFile metaFile) {
		String[] arguments;
		if(usePortZero)
			arguments = new String[] {"-http.port=:0", "-https.port=:0"};
		else
			arguments = new String[0];
		
		PrivateTestConfig testConfig = new PrivateTestConfig(platformOverrides, appOverrides, metaFile, true);
		init(testConfig, arguments);
	}
	
	public PrivateWebserverForTest(Module platformOverrides, Module appOverrides, VirtualFile metaFile, String ...args) {
		PrivateTestConfig testConfig = new PrivateTestConfig(platformOverrides, appOverrides, metaFile, true);
		init(testConfig, args);
	}
	
	public PrivateWebserverForTest(PrivateTestConfig testConfig) {
		String[] args = {"-hibernate.persistenceunit=webpieces-persistence"};
		init(testConfig, args);
	}

	private void init(PrivateTestConfig testConfig, String ... args) {
		//read here and checked for correctness on last line of server construction
		Arguments arguments = new CommandLineParser().parse(args);
		
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		SSLEngineFactoryWebServerTesting sslFactory = new SSLEngineFactoryWebServerTesting();
		HttpSvrInstanceConfig httpConfig = new HttpSvrInstanceConfig(null, (s) -> configure(s));
		HttpSvrInstanceConfig httpsConfig = new HttpSvrInstanceConfig(sslFactory, (s) -> configure(s));
		
		File baseWorkingDir = FileFactory.getBaseWorkingDir();

		//3 pieces to the webserver so a configuration for each piece
		WebServerConfig config = new WebServerConfig()
				.setPlatformOverrides(testConfig.getPlatformOverrides())
				.setHttpConfig(httpConfig)
				.setHttpsConfig(httpsConfig);
		RouterConfig routerConfig = new RouterConfig(baseWorkingDir)
											.setMetaFile(testConfig.getMetaFile() )
											.setWebappOverrides(testConfig.getAppOverrides())
											.setFileEncoding(CHAR_SET_TO_USE)
											.setDefaultResponseBodyEncoding(CHAR_SET_TO_USE)
											.setCachedCompressedDirectory(cacheDir)
											.setSecretKey(SecretKeyInfo.generateForTest())
											.setTokenCheckOn(testConfig.isUseTokenCheck());
		TemplateConfig templateConfig = new TemplateConfig();
		
		webServer = WebServerFactory.create(config, routerConfig, templateConfig, arguments);
		
		arguments.checkConsumedCorrectly();
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
