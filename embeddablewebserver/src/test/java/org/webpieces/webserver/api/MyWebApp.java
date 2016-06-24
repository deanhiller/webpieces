package org.webpieces.webserver.api;

import java.io.File;
import java.net.InetSocketAddress;

import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Module;

public class MyWebApp {
	
	private Module platformOverrides;
	private Module webappOverrides;
	
	public static void main(String[] args) throws InterruptedException {

		new MyWebApp(null, null).start();
		
		synchronized (MyWebApp.class) {
			//wait forever for now so server doesn't shut down..
			MyWebApp.class.wait();
		}	
	}

	public MyWebApp(Module platformOverrides, Module webappOverrides) {
		this.platformOverrides = platformOverrides;
		this.webappOverrides = webappOverrides;
	}

	public void start() throws InterruptedException {
//		String filePath1 = System.getProperty("user.dir");
//		VirtualFile srcDir = new VirtualFileImpl(filePath1+"/src/test/java");
//		CompileConfig devConfig = new CompileConfig(srcDir);
//		Module platformOverrides = new DevModule(devConfig);
		
		String filePath = System.getProperty("user.dir");
		File routerFile = new File(filePath + "/../embeddablewebserver/src/test/resources/routermodule.txt");
		VirtualFile configFile = new VirtualFileImpl(routerFile);

		HttpRouterConfig routerConfig = new HttpRouterConfig()
											.setRoutersFile(configFile )
											.setWebappOverrides(webappOverrides);
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(platformOverrides)
										.setHttpListenAddress(new InetSocketAddress(8080))
										.setHttpsListenAddress(new InetSocketAddress(8443));
		WebServer webServer = WebServerFactory.create(config, routerConfig);
		
		webServer.start();
			
	}
	
}
