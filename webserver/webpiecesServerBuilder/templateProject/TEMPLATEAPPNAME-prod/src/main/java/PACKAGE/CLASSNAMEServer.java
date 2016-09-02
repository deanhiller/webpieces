package PACKAGE;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import PACKAGE.example.CLASSNAMERouteModule;
import PACKAGE.example.tags.TagLookupOverride;

/**
 * Except for changing CLASSNAMEMeta inner class, changes to this one bootstrap class
 * and any classes you refer to here WILL require a restart when you are running the 
 * DevelopmentServer.  This class should try to remain pretty thin.
 * 
 * @author dhiller
 *
 */
public class CLASSNAMEServer {
	
	//This is where the list of Guice Modules go as well as the list of RouterModules which is the
	//core of anything you want to plugin to your web app.  To make re-usable components, you create
	//GuiceModule paired with a RouterModule and app developers can plug both in here.  In some cases,
	//only a RouterModule is needed and in others only a GuiceModule is needed.
	//BIG NOTE: The webserver loads this class from the appmeta.txt file which is passed in the
	//start method below.  This is a hook for the Development server to work that is a necessary evil
	public static class CLASSNAMEMeta implements WebAppMeta {

		//When using the Development Server, changes to this inner class will be recompiled automatically
		//when needed..  Changes to the outer class will not take effect until a restart
		//In production, we don't have a compiler on the classpath nor any funny classloaders so that
		//production is very very clean and the code for this non-dev server is very easy to step through
		//if you have a production issue
		public List<Module> getGuiceModules() {
			return Lists.newArrayList(new CLASSNAMEGuiceModule());
		}
		
		public List<RouteModule> getRouteModules() {
			return Lists.newArrayList(new CLASSNAMERouteModule());
		}

		//ALL APPLICATION plugins are added to classpath as a jar and then depending on the plugin has a RouteModule
		//and/or a GuiceModule depending on the plugin that you configure and add to your list of modules
		//in this inner class.  Then there are platform plugins which are plugged in using the main methods below
	}
	
	
	/*******************************************************************************
	 * When running the dev server, changes below this line require a server restart(you can try not to but it won't work)
	 * Changes above this line in the CLASSNAMEMeta inner class WILL work....those changes will be recompiled and
	 * loaded
	 *******************************************************************************/
	
	private static final Logger log = LoggerFactory.getLogger(CLASSNAMEServer.class);
	
	public static final Charset ALL_FILE_ENCODINGS = StandardCharsets.UTF_8;
	
	//Welcome to YOUR main method as webpieces webserver is just a library you use that you can
	//swap literally any piece of
	public static void main(String[] args) throws InterruptedException {
		CLASSNAMEServer server = new CLASSNAMEServer(null, null, new ServerConfig());
		
		server.start();
		
		synchronized (CLASSNAMEServer.class) {
			//wait forever so server doesn't shut down..
			CLASSNAMEServer.class.wait();
		}	
	}

	private WebServer webServer;

	public CLASSNAMEServer(Module platformOverrides, Module appOverrides, ServerConfig svrConfig) {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);

		URL resource = CLASSNAMEServer.class.getResource("/logback.xml");
		if(resource == null) {
			//Because the gradle application plugin doesn't set user.dir correctly, we have to do some stuff
			//here to run in an IDE
			if(!filePath.endsWith("/APPNAME-prod"))
				throw new IllegalStateException("Server script must be run from APPNAME-prod directory as in run ./bin/APPNAME-prod");
		} else if(platformOverrides == null && svrConfig.getHttpPort() != 0) {
			throw new IllegalStateException("This class only runs in production.  Use CLASSNAMESemiProdServer instead and"
					+ " it will use production router with a on-demand template compile engine OR "
					+ "use CLASSNAMEDevServer and it will recompile all your code on-demand as you change it");
		}

		VirtualFile metaFile = svrConfig.getMetaFile();
		//Dev server has to override this
		if(metaFile == null)
			metaFile = new VirtualFileClasspath("appmeta.txt", CLASSNAMEServer.class.getClassLoader());

		//This override is only needed if you want to add your own Html Tags to re-use
		Module allOverrides = new TagLookupOverride();
		if(platformOverrides != null) {
			allOverrides = Modules.combine(platformOverrides, allOverrides);
		}
		
		//Different pieces of the server have different configuration objects where settings are set
		//You could move these to property files but definitely put some thought if you want people 
		//randomly changing those properties and restarting the server without going through some testing
		//by a QA team.  We leave most of these properties right here.
		RouterConfig routerConfig = new RouterConfig()
											.setMetaFile(metaFile )
											.setFileEncoding(ALL_FILE_ENCODINGS) //appmeta.txt file encoding
											.setWebappOverrides(appOverrides)
											.setSecretKey("_SECRETKEYHERE_");
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(allOverrides)
										.setDefaultResponseBodyEncoding(ALL_FILE_ENCODINGS)
										.setHttpListenAddress(new InetSocketAddress(svrConfig.getHttpPort()))
										.setHttpsListenAddress(new InetSocketAddress(svrConfig.getHttpsPort()))
										.setFunctionToConfigureServerSocket(s -> configure(s))
										.setValidateRouteIdsOnStartup(svrConfig.isValidateRouteIdsOnStartup());
		TemplateConfig templateConfig = new TemplateConfig();
		
		webServer = WebServerFactory.create(config, routerConfig, templateConfig);
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
	
	public HttpRequestListener start() {
		return webServer.start();	
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
