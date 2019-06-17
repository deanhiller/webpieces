package WEBPIECESxPACKAGE;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
import com.google.inject.util.Modules;

import WEBPIECESxPACKAGE.base.tags.TagLookupOverride;

/**
 * Changes to any class in this package (or any classes these classes reference) WILL require a 
 * restart when you are running the DevelopmentServer.  This class should try to remain pretty
 * thin and you should avoid linking any classes in this package to classes outside this
 * package(This is only true if you want to keep using the development server).  In production,
 * we do not play any classloader games at all(unlike play framework) avoiding any prod issues.
 */
public class Server {
	
	/*******************************************************************************
	 * When running the dev server, changes to this file AND to any files in this package
	 * require a server restart(you can try not to but it won't work)
	 *******************************************************************************/
	
	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	public static final Charset ALL_FILE_ENCODINGS = StandardCharsets.UTF_8;
	
	//Welcome to YOUR main method as webpieces webserver is just a library you use that you can
	//swap literally any piece of
	public static void main(String[] args) throws InterruptedException {
		try {
			Server server = new Server(null, null, new ServerConfig("production"));
			
			server.start();
			
			synchronized (Server.class) {
				//wait forever so server doesn't shut down..
				Server.class.wait();
			}	
		} catch(Throwable e) {
			log.error("Failed to startup.  exiting jvm", e);
			System.exit(1); // should not be needed BUT some 3rd party libraries start non-daemon threads :(
		}
	}
	
	private WebServer webServer;

	public Server(
			Module platformOverrides, 
			Module appOverrides, 
			ServerConfig svrConfig) {
		String filePath = System.getProperty("user.dir");
		log.info("original user.dir before modification="+filePath);

		File baseWorkingDir = modifyUserDirForManyEnvironments(filePath);

		VirtualFile metaFile = svrConfig.getMetaFile();
		//Dev server has to override this
		if(metaFile == null)
			metaFile = new VirtualFileClasspath("appmeta.txt", Server.class.getClassLoader());

		if(!metaFile.exists())
			throw new RuntimeException("file not found="+metaFile);
		
		//This override is only needed if you want to add your own Html Tags to re-use
		//you can delete this code if you are not adding your own html tags
		Module allOverrides = new TagLookupOverride();
		if(platformOverrides != null) {
			allOverrides = Modules.combine(platformOverrides, allOverrides);
		}
		
		SecretKeyInfo signingKey = new SecretKeyInfo(fetchKey(), "HmacSHA1");
		
		WebSSLFactory webSSLFactory = new WebSSLFactory();

		//Different pieces of the server have different configuration objects where settings are set
		//You could move these to property files but definitely put some thought if you want people 
		//randomly changing those properties and restarting the server without going through some testing
		//by a QA team.  We leave most of these properties right here so changes get tested by QA.
		
		//A SECOND note is that some properties can be modified at runtime and so some config objects could be exposed
		//through JMX or other means for dynamically changing things at runtime
		RouterConfig routerConfig = new RouterConfig(baseWorkingDir)
											.setMetaFile(metaFile)
											.setWebappOverrides(appOverrides)
											.setWebAppMetaProperties(svrConfig.getWebAppMetaProperties())
											.setSecretKey(signingKey)
											.setPortConfigCallback(() -> fetchPortsForRedirects())
											.setCachedCompressedDirectory(svrConfig.getCompressionCacheDir())
											.setNeedsSimpleStorage(webSSLFactory)
											.setTokenCheckOn(svrConfig.isTokenCheckOn()); 
				
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(allOverrides)
										.setHttpListenAddress(new InetSocketAddress(svrConfig.getHttpPort()))
										.setHttpsListenAddress(new InetSocketAddress(svrConfig.getHttpsPort()))
										.setSslEngineFactory(webSSLFactory)
										.setFunctionToConfigureServerSocket(s -> configure(s))
										.setValidateRouteIdsOnStartup(svrConfig.isValidateRouteIdsOnStartup())
										.setStaticFileCacheTimeSeconds(svrConfig.getStaticFileCacheTimeSeconds());
		TemplateConfig templateConfig = new TemplateConfig();
		
		webServer = WebServerFactory.create(config, routerConfig, templateConfig);
	}
	
	PortConfig fetchPortsForRedirects() {
		//NOTE: You will need to modify this so it detects when you are behind a firewall that has ports exposed to 
		//customers different than the ports your server exposes
		boolean useFirewallPorts = false;
		
		//NOTE: for running locally and for tests, you must set useFirewallPorts=false
		
		int httpPort = 80; //good security teams generally have the firewall on port 80 and your server on something like 8080
		int httpsPort = 443; //good security teams generally have the firewall on port 443 and your server on something like 8443
		if(!useFirewallPorts) {
			//otherwise use the same port the webserver is bound to
			//this is for running locally AND for local tests
			httpPort = getUnderlyingHttpChannel().getLocalAddress().getPort();
			httpsPort = getUnderlyingHttpsChannel().getLocalAddress().getPort();
		}
		return new PortConfig(httpPort, httpsPort);
	}
	
	private byte[] fetchKey() {
		//This is purely so it works before template creation
		//NOTE: our build runs all template tests that are generated to make sure we don't break template 
		//generation but for that to work pre-generation, we need this code but you are free to delete it...
		String base64Key = "__SECRETKEYHERE__";  //This gets replaced with a unique key each generated project which you need to keep or replace with your own!!!
		if(base64Key.startsWith("__SECRETKEY"))  //This does not get replaced (user can remove it from template)
			return base64Key.getBytes();
		return Base64.getDecoder().decode(base64Key);
	}

	private File modifyUserDirForManyEnvironments(String filePath) {
		File absPath = FileFactory.newAbsoluteFile(filePath);
		File finalUserDir = modifyUserDirForManyEnvironmentsImpl(absPath);
		log.info("RECONFIGURED working directory(based off user.dir)="+finalUserDir.getAbsolutePath()+" previous user.dir="+filePath);
		return finalUserDir;
	}

	/**
	 * I like things to work seamlessly but user.dir is a huge issue in multiple environments...and Intellij makes it
	 * harder by giving servers a different user.dir than tests even though they are in the same subproject!!
	 *
	 * Format of comments BELOW in if/else statements is like this
	 *
	 * {type}-{isWebpieces}-{IDE or Container}-{subprojectName}
	 *
	 * where type=Test or MainApp (Intellij changes the user.dir for tests vs. mainapp!!  DAMNIT Intellij)
	 * IDE=Intellij, Eclipse, Gradle, Production
	 * isWebpieces is whether it was a generated project or is the template itself.  ie. you can run tests
	 *     if you clone https://github.com/deanhiller/webpieces inside the IDE without needing to
	 *     generate a fake project BUT we need to know which directory it runs for (MAINLY Intellij screwup again)
	 *     isWebpieces is a major convenience for webpieces developers to test changes to templates and
	 *     debug them but DOES NOT need to be part of your project actually so could be deleted.
	 */
	private File modifyUserDirForManyEnvironmentsImpl(File filePath) {
		if(!filePath.isAbsolute())
			throw new IllegalArgumentException("If filePath is not absolute, you will have trouble working in all environments in the comment above. path="+filePath.getPath());

		String name = filePath.getName();

		File locatorFile1 = FileFactory.newFile(filePath, "locatorFile.txt");
		File locatorFile2 = FileFactory.newFile(filePath, "xLocatorFile.txt");

		File bin = FileFactory.newFile(filePath, "bin");
		File lib = FileFactory.newFile(filePath, "lib");
		File config = FileFactory.newFile(filePath, "config");
		File publicFile = FileFactory.newFile(filePath, "public");
		if(bin.exists() && lib.exists() && config.exists() && publicFile.exists()) {
			//For ->
			//    MainApp | NO  | Production | N/A
			log.info("Running in production environment");
			return filePath;
		} else if("WEBPIECESxAPPNAME-dev".equals(name)) {
			//    Test    | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | YES | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | NO  | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | YES | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    MainApp | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    MainApp | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			log.info("You appear to be running test from Intellij or Gradle(xxxx-dev subproject).");
			File parent = filePath.getParentFile();
			return FileFactory.newFile(parent, "WEBPIECESxAPPNAME/src/dist");
		} else if("WEBPIECESxAPPNAME".equals(name)) {
			//    Test    | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | YES | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | NO  | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | YES | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			log.info("You appear to be running test from Intellij or Gradle(main subproject).");
			return FileFactory.newFile(filePath, "src/dist");
		} else if(locatorFile1.exists()) {
			//DAMNIT Intellij...FIX THIS STUFF!!!
			//For ->
			//    MainApp | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			log.info("You appear to be running a main app from Intellij..but unclear from which subproject");
			return FileFactory.newFile(filePath, "WEBPIECESxAPPNAME/src/dist");
		} else if(locatorFile2.exists()) {
			//DAMNIT Intellij...FIX THIS STUFF!!!
			//
			//    MainApp | YES | Intellij    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | YES | Intellij    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			log.info("Running DevServer in Intellij, making property modifications(damn intellij..fix that)");
			return FileFactory.newFile(filePath, "webserver/webpiecesServerBuilder/templateProject/WEBPIECESxAPPNAME/src/dist");
		}

		throw new IllegalStateException("bug, we must have missed an environment="+name+" full path="+filePath);
	}

	/**
	 * This is a bit clunky BUT if jdk authors add methods that you can configure, we do not have
	 * to change our platform every time so you can easily set the new properties rather than waiting for
	 * us to release a new version 
	 */
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
}
