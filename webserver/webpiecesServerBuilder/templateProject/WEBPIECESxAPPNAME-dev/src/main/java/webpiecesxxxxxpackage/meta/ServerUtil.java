package webpiecesxxxxxpackage.meta;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webpiecesxxxxxpackage.DevServer;

public class ServerUtil {

	private static final Logger log = LoggerFactory.getLogger(ServerUtil.class);

	public static void start(Supplier<DevServer> function) {
		try {
			String version = System.getProperty("java.version");
			DevServer server = function.get();
			log.info("Starting "+server.getClass().getSimpleName()+" under java version="+version);

			server.start();
			
			//Since we typically use only 3rd party libraries with daemon threads, that means this
			//main thread is the ONLY non-daemon thread letting the server keep running so we need
			//to block it and hold it up from exiting.  Modify this to release if you want an ability
			//to remotely shutdown....
			synchronized(DevServer.class) {
				DevServer.class.wait();
			}
		} catch(Throwable e) {
			log.error("Failed to startup.  exiting jvm. msg="+e.getMessage(), e);
			System.exit(1); // should not be needed BUT some 3rd party libraries start non-daemon threads :(
		}		
	}

}
