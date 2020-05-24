package webpiecesxxxxxpackage;

import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.Module;

import webpiecesxxxxxpackage.meta.JavaCache;
import webpiecesxxxxxpackage.meta.ServerUtil;

/**
 * Uses the production Router but the dev template compiler so you 'could' step through prod router code
 * to see if something is going on
 * 
 * TODO: modify this to compile ALL gradle groovy template *.class files (ones generated from the html) and then we
 * could run full production mode from the IDE
 */
public class ProdServerForIDE extends DevServer {
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		ServerUtil.start(() -> new ProdServerForIDE(false));
	}
	
	private Server server;

	public ProdServerForIDE(boolean usePortZero) {
		super(usePortZero);
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths);

		//swap components for on-demand template compile only and the rest behaves like prod ONLY
		//because you don't want to have to run gradle on every change(instead just restart server!)
		Module platformOverrides = new DevTemplateModule(templateConfig);

		ServerConfig config = new ServerConfig(JavaCache.getCacheLocation(), false);
		//It is very important to turn off caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		
		server = new Server(platformOverrides, null, config, args );
	}
	

	
	public void start() {
		server.start();		
	}

}
