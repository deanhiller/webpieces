package PACKAGE;

import java.io.File;
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

public class CLASSNAMEServer {
	
	private static final Logger log = LoggerFactory.getLogger(CLASSNAMEServer.class);
	private Module platformOverrides;
	private Module appOverrides;
	
	//This is where the list of Guice Modules go as well as the list of RouterModules which is the
	//core of anything you want to plugin to your web app.  To make re-usable components, you create
	//GuiceModule paired with a RouterModule and app developers can plug both in here.  In some cases,
	//only a RouterModule is needed and in others only a GuiceModule is needed.
	public static class CLASSNAMEMeta implements WebAppMetaInfo {

		public List<Module> getGuiceModules() {
			return Lists.newArrayList(new CLASSNAMEModule());
		}
		
		public List<RouteModule> getRouterModules() {
			return Lists.newArrayList(new CLASSNAMERoutes());
		}

	}
	
	public static void main(String[] args) throws InterruptedException {
		new CLASSNAMEServer(null, null).start();
		
		synchronized (CLASSNAMEServer.class) {
			//wait forever for now so server doesn't shut down..
			CLASSNAMEServer.class.wait();
		}	
	}

	public CLASSNAMEServer(Module platformOverrides, Module appOverrides) {
		this.platformOverrides = platformOverrides;
		this.appOverrides = appOverrides;
	}

	public void start() throws InterruptedException {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		//This is so dev-server works.....
		File routerFile = new File(filePath + "/../ZAPPNAME/src/main/resources/appmeta.txt");
		VirtualFile configFile = new VirtualFileImpl(routerFile);

		HttpRouterConfig routerConfig = new HttpRouterConfig()
											.setRoutersFile(configFile )
											.setWebappOverrides(appOverrides);
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(platformOverrides)
										.setHttpListenAddress(new InetSocketAddress(8080))
										.setHttpsListenAddress(new InetSocketAddress(8443));
		WebServer webServer = WebServerFactory.create(config, routerConfig);
		
		webServer.start();	
	}

}
