package org.webpieces.webserver.api;

import java.io.File;

import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

public class MyWebApp {
	
	public static void main(String[] args) throws InterruptedException {
		String filePath = System.getProperty("user.dir");
		File routerFile = new File(filePath + "/src/test/resources/routermodule.txt");
		VirtualFile configFile = new VirtualFileImpl(routerFile);
		
		HttpRouterConfig routerConfig = new HttpRouterConfig().setRoutersFile(configFile );
		WebServerConfig config = new WebServerConfig();
		WebServer webServer = WebServerFactory.create(config, routerConfig);
		
		webServer.start();
		
		synchronized (MyWebApp.class) {
			//wait forever for now so server doesn't shut down..
			MyWebApp.class.wait();
		}
		
	}
	
}
