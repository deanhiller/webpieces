package webpiecesxxxxxpackage.services;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

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
			final ClassLoader classLoaderOfLoggerFactory = LoggerFactory.class.getClassLoader();
			ServiceLoader<SLF4JServiceProvider> serviceLoader = ServiceLoader.load(SLF4JServiceProvider.class, classLoaderOfLoggerFactory);
			Iterator<SLF4JServiceProvider> iterator = serviceLoader.iterator();
			SLF4JServiceProvider p = null;
			while(iterator.hasNext()) {
				if(p != null)
					throw new IllegalArgumentException("Two logging implementations found, fix this first");
				p = iterator.next();
			}

			MDCAdapter mdcAdapter = p.getMDCAdapter();


			String version = System.getProperty("java.version");
			YourCompanyAbstractDevServer server = function.get();
			log.info("Starting "+server.getClass().getSimpleName()+" under java version="+version);

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
