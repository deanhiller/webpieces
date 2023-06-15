package webpiecesxxxxxpackage.services;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public class DevServerUtil {

	private static final Logger log = LoggerFactory.getLogger(DevServerUtil.class);

	public static void start(Supplier<YourCompanyAbstractDevServer> function) {
		try {

			String version = System.getProperty("java.version");
			log.info("Starting under java version="+version);

			YourCompanyAbstractDevServer server = function.get();

			server.start();
			
			//Since we typically use only 3rd party libraries with daemon threads, that means this
			//main thread is the ONLY non-daemon thread letting the server keep running so we need
			//to block it and hold it up from exiting.  Modify this to release if you want an ability
			//to remotely shutdown....
			synchronized(YourCompanyAbstractDevServer.class) {
				YourCompanyAbstractDevServer.class.wait();
			}
		} catch(Throwable e) {
			log.error("Failed to startup.  exiting jvm.", e);
			System.exit(1); // should not be needed BUT some 3rd party libraries start non-daemon threads :(
		}		
	}

}
