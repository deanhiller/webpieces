package webpiecesxxxxxpackage;

import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.Module;

import webpiecesxxxxxpackage.services.DevConfig;
import webpiecesxxxxxpackage.services.DevServerUtil;
import webpiecesxxxxxpackage.services.YourCompanyProdServerForIDE;

/**
 * Uses the production Router but the dev template compiler so you 'could' step through prod router code
 * to see if something is going on
 * 
 * TODO: modify this to compile ALL gradle groovy template *.class files (ones generated from the html) and then we
 * could run full production mode from the IDE
 */
public class ProdServerForIDE extends YourCompanyProdServerForIDE {
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		DevServerUtil.start(() -> new ProdServerForIDE(false));
	}
	
	public ProdServerForIDE(boolean usePortZero) {
		super("WEBPIECESxAPPNAME", usePortZero);
	}

	@Override
	protected YourCompanyServer createServer(Module platformOverrides, ServerConfig config, String ... args) {
		return new Server(platformOverrides, null, config, args);
	}

	@Override
	protected DevConfig getConfig() {
		return new OurDevConfig();
	}
}
