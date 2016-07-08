package PACKAGE;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;

/**
 * Except for changing CLASSNAMEMeta inner class, changes to this one bootstrap class
 * and any classes you refer to here WILL require a restart when you are running the 
 * DevelopmentServer.  This class should try to remain pretty thin.
 * 
 * @author dhiller
 *
 */
public class CLASSNAMEServer {
	
	private static final Logger log = LoggerFactory.getLogger(CLASSNAMEServer.class);
	
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
			return Lists.newArrayList(new CLASSNAMEModule());
		}
		
		public List<RouteModule> getRouteModules() {
			return Lists.newArrayList(new CLASSNAMERouteModule());
		}

		//ALL APPLICATION plugins are added to classpath as a jar and then depending on the plugin has a RouteModule
		//and/or a GuiceModule depending on the plugin that you configure and add to your list of modules
		//in this inner class.  Then there are platform plugins which are plugged in using the main methods below
	}
	
	//Welcome to YOUR main method as webpieces webserver is just a library you use that you can
	//swap literally any piece of
	public static void main(String[] args) throws InterruptedException {
		new CLASSNAMEServer(null, null, false).start();
		
		synchronized (CLASSNAMEServer.class) {
			//wait forever for now so server doesn't shut down..
			CLASSNAMEServer.class.wait();
		}	
	}

	private WebServer webServer;

	public CLASSNAMEServer(Module platformOverrides, Module appOverrides, boolean usePortZero) {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		//This is so dev-server works.....
		VirtualFile metaFile = new VirtualFileImpl(filePath + "/../TEMPLATEAPPNAME-prod/src/main/resources/appmeta.txt");

		//Different pieces of the server have different configuration objects where settings are set
		//You could move these to property files but definitely put some thought if you want people 
		//randomly changing those properties and restarting the server without going through some testing
		//by a QA team
		HttpRouterConfig routerConfig = new HttpRouterConfig()
											.setMetaFile(metaFile )
											.setWebappOverrides(appOverrides);
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(platformOverrides)
										.setHttpListenAddress(new InetSocketAddress(8080))
										.setHttpsListenAddress(new InetSocketAddress(8443))
										.setFunctionToConfigureServerSocket(s -> configure(s));
		
		if(usePortZero) {
			config.setHttpListenAddress(new InetSocketAddress(0));
			config.setHttpsListenAddress(new InetSocketAddress(0));
		}
		
		webServer = WebServerFactory.create(config, routerConfig);
	}

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

}
