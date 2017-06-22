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

		modifyUserDirForManyEnvironments(filePath);

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
		
		//Different pieces of the server have different configuration objects where settings are set
		//You could move these to property files but definitely put some thought if you want people 
		//randomly changing those properties and restarting the server without going through some testing
		//by a QA team.  We leave most of these properties right here.
		
		//A SECOND note is that some properties can be modified at runtime and so some config objects could be exposed
		//through JMX or other means for dynamically changing things at runtime
		RouterConfig routerConfig = new RouterConfig()
											.setMetaFile(metaFile)
											.setWebappOverrides(appOverrides)
											.setWebAppMetaProperties(svrConfig.getWebAppMetaProperties())
											.setSecretKey(signingKey)
											.setPortConfigCallback(() -> fetchPortsForRedirects());
		
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(allOverrides)
										.setHttpListenAddress(new InetSocketAddress(svrConfig.getHttpPort()))
										.setHttpsListenAddress(new InetSocketAddress(svrConfig.getHttpsPort()))
										.setSslEngineFactory(new WebSSLFactory())
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

	private void modifyUserDirForManyEnvironments(String filePath) {
		String finalUserDir = modifyUserDirForManyEnvironmentsImpl(filePath);
		System.setProperty("user.dir", finalUserDir);
		log.info("RECONFIGURED user.dir="+finalUserDir);
	}

	/**
	 * I like things to work seamlessly but user.dir is a huge issue in multiple environments I am working in.
	 * main server has to work in N configurations that should be tested and intellij is a PITA since
	 * it is inconsistent.  PP means runs in the project the file is in and eclipse is consistent with
	 * gradle while intellij is only half the time....
	 * 
	 * app dev - The environment when you generate a project and import it into an IDE
	 * webpieces - The environment where you test the template directly when bringing in webpieces into an IDE
	 * 
	 * 
	 * * app dev / eclipse -
	 *    * PP - running myapp/src/tests - user.dir=myapp-all/myapp
	 *    * PP - DevServer - user.dir=myapp-all/myapp-dev
	 *    * PP - SemiProductionServer - user.dir=myapp-all/myapp-dev
	 *    * PP - ProdServer - user.dir=myapp-all/myapp
	 * * app dev / intellij (it's different paths than eclipse :( ).  user.dir starts as myapp directory
	 *    * PP - running myapp/src/tests - myapp-all/myapp
	 *    * NO - DevServer - user.dir=myapp-all :( what the hell!  different from running tests
	 *    * NO - SemiProductionServer - user.dir=myapp-all
	 *    * NO - ProdServer - user.dir=myapp-all
	 * * webpieces / eclipse - same as app dev because eclipse is nice in this aspect
	 * * webpieces / intellij - ANNOYING and completely different.  Runs out of webpieces a few levels down from actual subproject
	 * * PP - tests in webpieces gradle - myapp-all/myapp
	 * * PP - tests in myapp's gradle run - myapp-all/myapp
	 * * NO - production - user.dir=from distribution myapp directory which has subdirs bin, lib, config, public
	 * * Future? - run DevSvr,SemiProdSvr,ProdSvr from gradle?....screw that for now..it's easy to run from IDE so why bother(it may just work though too)
	 * 
	 * - so in production, the relative paths work from myapp so 'public/' is a valid location for html files resolving to myapp/public
	 * - in testing, IF we want myapp-all/myapp/src/dist/public involved, it would be best to run from myapp-all/myapp/src/dist so 'public/' is still a valid location
	 * - in devserver, semiprodserver, and prod server, the same idea follows where myapp-all/myapp/src/dist should be the user.dir!!!
	 * 
	 * - sooooo, algorithm is this
	 * - if user.dir=myapp-all, modify user.dir to myapp-all/myapp/src/dist (you are in intellij)
	 * - else if user.dir=myapp-dev, modify to ../myapp/src/dist
	 * - else if myapp has directories bin, lib, config, public then do nothing
	 * - else modify user.dir=myapp to myapp/src/dist
	 */
	private String modifyUserDirForManyEnvironmentsImpl(String filePath) {
		File f = new File(filePath);
		String name = f.getName();
		if("WEBPIECESxAPPNAME-all".equals(name)) {
			return new File(filePath, "WEBPIECESxAPPNAME/src/dist").getAbsolutePath();
		} else if("WEBPIECESxAPPNAME-dev".equals(name)) {
			File parent = f.getParentFile();
			return new File(parent, "WEBPIECESxAPPNAME/src/dist").getAbsolutePath();
		} else if(!"WEBPIECESxAPPNAME".equals(name)) {
			if(filePath.endsWith("WEBPIECESxAPPNAME/src/dist"))
				return filePath; //This occurs when a previous test ran already and set user.dir
			else if(filePath.endsWith("webpieces")) //
				return filePath+"/webserver/webpiecesServerBuilder/templateProject/WEBPIECESxAPPNAME/src/dist";
			throw new IllegalStateException("bug, we must have missed an environment="+name);
		}
		
		File bin = new File(f, "bin");
		File lib = new File(f, "lib");
		File config = new File(f, "config");
		File publicFile = new File(f, "public");
		if(bin.exists() && lib.exists() && config.exists() && publicFile.exists()) {
			return filePath;
		}
		
		return new File(f, "src/dist").getAbsolutePath();
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
