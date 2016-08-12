package org.webpieces.webserver;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.inject.Module;

public class WebserverForTest {
	
	private static final Logger log = LoggerFactory.getLogger(WebserverForTest.class);
	public static final Charset CHAR_SET_TO_USE = StandardCharsets.UTF_8;
	
	public static void main(String[] args) throws InterruptedException {
		new WebserverForTest(null, null, false, null).start();
		
		synchronized (WebserverForTest.class) {
			//wait forever for now so server doesn't shut down..
			WebserverForTest.class.wait();
		}	
	}

	private WebServer webServer;

	public WebserverForTest(Module platformOverrides, Module appOverrides, boolean usePortZero, VirtualFile metaFile) {
		String filePath = System.getProperty("user.dir");
		log.info("property user.dir="+filePath);
		
		//Tests can override this...
		if(metaFile == null)
			metaFile = new VirtualFileClasspath("basicMeta.txt", WebserverForTest.class.getClassLoader());
		
		int httpPort = 8080;
		int httpsPort = 8443;
		if(usePortZero) {
			httpPort = 0;
			httpsPort = 0;
		}
		
		HttpRouterConfig routerConfig = new HttpRouterConfig()
											.setMetaFile(metaFile )
											.setWebappOverrides(appOverrides)
											.setFileEncoding(CHAR_SET_TO_USE);
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(platformOverrides)
										.setHtmlResponsePayloadEncoding(CHAR_SET_TO_USE)
										.setHttpListenAddress(new InetSocketAddress(httpPort))
										.setHttpsListenAddress(new InetSocketAddress(httpsPort))
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
