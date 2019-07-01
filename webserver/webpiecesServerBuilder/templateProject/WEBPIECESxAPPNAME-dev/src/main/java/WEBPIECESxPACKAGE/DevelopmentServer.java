package WEBPIECESxPACKAGE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevelopmentServer {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		String version = System.getProperty("java.version");
		log.info("Starting Development Server under java version="+version);
		log.atInfo().log(() -> "testing out %caller{1}");
		log.atInfo().log("another test here as well");

		return;
	}
	
}
