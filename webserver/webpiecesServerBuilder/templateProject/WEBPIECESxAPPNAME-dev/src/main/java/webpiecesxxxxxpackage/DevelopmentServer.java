package webpiecesxxxxxpackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import webpiecesxxxxxpackage.meta.JavaCache;
import webpiecesxxxxxpackage.meta.ServerUtil;

public class DevelopmentServer extends DevServer {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		ServerUtil.start(() -> new DevelopmentServer(false));
	}
	
	private Server server;

	public DevelopmentServer(boolean usePortZero) {
		super(usePortZero);
		
		VirtualFile metaFile = directory.child("WEBPIECESxAPPNAME/src/main/resources/appmetadev.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());

		Module platformOverrides = swapComponentsForDevServer();
		
		ServerConfig config = new ServerConfig(JavaCache.getCacheLocation(), false);
		//It is very important to turn off BROWSER caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		config.setMetaFile(metaFile);
		
		server = new Server(platformOverrides, null, config, args);
	}

	private Module swapComponentsForDevServer() {
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths);
		
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths, CompileConfig.getHomeCacheDir("WEBPIECESxAPPNAMECache/devserver-bytecode"));
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new DevTemplateModule(templateConfig));
		return platformOverrides;
	}
	
	public void start() {
		server.start();		
	}

	public void stop() {
		server.stop();
	}

}
