package WEBPIECESxPACKAGE;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.api.WebpiecesServer;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import WEBPIECESxPACKAGE.base.PlatformOverrides;
import WEBPIECESxPACKAGE.base.RandomInstanceId;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Changes to any class in this 'package' (or any classes that classes in this 
 * package reference) WILL require a restart when you are running the DevelopmentServer.  
 * This class should try to remain pretty thin and you should avoid linking any 
 * classes in this package to classes outside this package(This is only true if 
 * you want to keep using the development server).  In production, we do not 
 * play any classloader games at all(unlike play framework) avoiding any prod issues.
 */
public class Server {

	/*******************************************************************************
	 * When running the dev server, changes to this file AND to any files in this package
	 * require a server restart(you can try not to but it won't work)
	 *******************************************************************************/
	
	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	public static final Charset ALL_FILE_ENCODINGS = StandardCharsets.UTF_8;
	
	/**
	 * Welcome to YOUR main method as webpieces webserver is just a LIBRARY you use that you can
	 * swap literally any piece of
	 */
	public static void main(String[] args) throws InterruptedException {
		try {
			String version = System.getProperty("java.version");
			log.info("Starting Production Server under java version="+version);

			//We typically move this to the command line so staging can have
			//-hibernate.persistenceunit=stagingdb instead but to help people startup, we add the arg
			String[] newArgs = addArgs(args, "-hibernate.persistenceunit=production");
			
			//You could pass in an instance id but in google cloud run, you have to generate it
			String instanceId = RandomInstanceId.generate();
			
			CompositeMeterRegistry metrics = new CompositeMeterRegistry();
			metrics.add(new SimpleMeterRegistry());
			//Add Amazon or google or other here.  This one is google's...
			//metrics.add(StackdriverMeterRegistry.builder(stackdriverConfig).build());

			ServerConfig svrConfig = createServerConfig();
			Server server = new Server(metrics, null, null, svrConfig, newArgs);
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

	private final WebpiecesServer webServer;

	public Server(
		MeterRegistry metrics,
		Module platformOverrides, 
		Module appOverrides, 
		ServerConfig svrConfig, 
		String ... args
	) {
		String base64Key = "__SECRETKEYHERE__";  //This gets replaced with a unique key each generated project which you need to keep or replace with your own!!!


		Module allOverrides = new PlatformOverrides(metrics);
		if(platformOverrides != null) {
			allOverrides = Modules.combine(platformOverrides, allOverrides);
		}

		webServer = new WebpiecesServer("WEBPIECESxAPPNAME", base64Key, allOverrides, appOverrides, svrConfig, args);
	}

	public void start() {
		webServer.start();		
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
	
	private static ServerConfig createServerConfig() {
		ServerConfig config = new ServerConfig(true);
		return config;
	}
	
	private static String[] addArgs(String[] originalArgs, String ... additionalArgs) {
		ArrayList<String> listArgs = Lists.newArrayList(originalArgs);
		for(String arg : additionalArgs) {
			listArgs.add(arg);
		}
		return listArgs.toArray(new String[0]);
	}

}
