package webpiecesxxxxxpackage;

import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.Module;

import webpiecesxxxxxpackage.basesvr.YourCompanyServer;
import webpiecesxxxxxpackage.services.DevConfig;
import webpiecesxxxxxpackage.services.DevServerUtil;
import webpiecesxxxxxpackage.services.YourCompanyDevelopmentServer;

public class DevelopmentServer extends YourCompanyDevelopmentServer {

	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		DevServerUtil.start(() -> new DevelopmentServer(false));
	}
	
	public DevelopmentServer(boolean usePortZero) {
		super("WEBPIECESxAPPNAME", usePortZero);
	}

	@Override
	protected YourCompanyServer createServer(Module platformOverrides, Module appOverrides, ServerConfig config, String... args) {
		return new Server(platformOverrides, null, config, args);
	}

	@Override
	protected DevConfig getConfig() {
		return new OurDevConfig();
	}
}
