package webpiecesxxxxxpackage;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.api.WebpiecesServer;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import webpiecesxxxxxpackage.base.PlatformOverrides;
import webpiecesxxxxxpackage.base.RandomInstanceId;

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
			String[] newArgs = addArgs(new String[] {"-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsProd", "-hibernate.loadclassmeta=true"});

			//You could pass in an instance id but in google cloud run, you have to generate it
			String instanceId = RandomInstanceId.generate();

			ServerConfig svrConfig = createServerConfig();
			Server server = new Server(new MetricsModule(instanceId), null, svrConfig, newArgs);
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
		Module platformOverrides, 
		Module appOverrides, 
		ServerConfig svrConfig, 
		String ... args
	) {
		String base64Key = "__SECRETKEYHERE__";  //This gets replaced with a unique key each generated project which you need to keep or replace with your own!!!

		Module allOverrides = new PlatformOverrides();
		if(platformOverrides != null) {
			allOverrides = Modules.combine(platformOverrides, allOverrides);
		}

		
		log.info("Constructing WebpiecesServer with args="+Arrays.asList(args));
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
