package org.webpieces.webserver.api.basic;

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

public class BasicWebserver {
	
	private static final Logger log = LoggerFactory.getLogger(BasicWebserver.class);
	
	public static class BasicAppMeta implements WebAppMeta {
		public List<Module> getGuiceModules() {
			return Lists.newArrayList(new BasicModule());
		}
		
		public List<RouteModule> getRouteModules() {
			return Lists.newArrayList(new BasicRouteModule());
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		new BasicWebserver(null, null).start();
		
		synchronized (BasicWebserver.class) {
			//wait forever for now so server doesn't shut down..
			BasicWebserver.class.wait();
		}	
	}

	private WebServer webServer;

	public BasicWebserver(Module platformOverrides, Module appOverrides) {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		//This is so dev-server works.....
		VirtualFile metaFile = new VirtualFileImpl(filePath + "/src/test/resources/basic.txt");

		HttpRouterConfig routerConfig = new HttpRouterConfig()
											.setMetaFile(metaFile )
											.setWebappOverrides(appOverrides);
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(platformOverrides)
										.setHttpListenAddress(new InetSocketAddress(8080))
										.setHttpsListenAddress(new InetSocketAddress(8443))
										.setFunctionToConfigureServerSocket(s -> configure(s));
		
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
