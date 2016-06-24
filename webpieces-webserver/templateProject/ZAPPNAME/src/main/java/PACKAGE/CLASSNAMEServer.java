package PACKAGE;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMetaInfo;
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
	public static class CLASSNAMEMeta implements WebAppMetaInfo {

		public List<Module> getGuiceModules() {
			return Lists.newArrayList(new CLASSNAMEModule());
		}
		
		public List<RouteModule> getRouterModules() {
			return Lists.newArrayList(new CLASSNAMERoutes());
		}

	}
	
	//Welcome to YOUR main method as webpieces webserver is just a library you use that you can
	//swap literally any piece of
	public static void main(String[] args) throws InterruptedException {
		new CLASSNAMEServer(null, null).start();
		
		synchronized (CLASSNAMEServer.class) {
			//wait forever for now so server doesn't shut down..
			CLASSNAMEServer.class.wait();
		}	
	}

	private Module platformOverrides;
	private Module appOverrides;
	
	public CLASSNAMEServer(Module platformOverrides, Module appOverrides) {
		this.platformOverrides = platformOverrides;
		this.appOverrides = appOverrides;
	}

	public void start() throws InterruptedException {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		//This is so dev-server works.....
		VirtualFile metaFile = new VirtualFileImpl(filePath + "/../ZAPPNAME/src/main/resources/appmeta.txt");

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
										.setHttpsListenAddress(new InetSocketAddress(8443));
		WebServer webServer = WebServerFactory.create(config, routerConfig);
		
		webServer.start();	
	}

}
