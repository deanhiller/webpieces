package WEBPIECESxPACKAGE;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.plugins.hsqldb.H2DbPlugin;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class WEBPIECESxCLASSDevServer {

	private static final Logger log = LoggerFactory.getLogger(WEBPIECESxCLASSServer.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		new WEBPIECESxCLASSDevServer(false).start();
		
		//Since we typically use only 3rd party libraries with daemon threads, that means this
		//main thread is the ONLY non-daemon thread letting the server keep running so we need
		//to block it and hold it up from exiting.  Modify this to release if you want an ability
		//to remotely shutdown....
		synchronized(WEBPIECESxCLASSDevServer.class) {
			WEBPIECESxCLASSDevServer.class.wait();
		}
	}
	
	private WEBPIECESxCLASSServer server;

	public WEBPIECESxCLASSDevServer(boolean usePortZero) {
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
        String directory = modifyForIDE(filePath1);
        
		//list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(directory+"/WEBPIECESxAPPNAME/src/main/java"));
		
		VirtualFile metaFile = new VirtualFileImpl(directory + "/WEBPIECESxAPPNAME/src/main/resources/appmeta.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths)
														.setFileEncoding(WEBPIECESxCLASSServer.ALL_FILE_ENCODINGS);
		
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths)
										.setFileEncoding(WEBPIECESxCLASSServer.ALL_FILE_ENCODINGS);
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new DevTemplateModule(templateConfig));
		
		List<WebAppMeta> plugins = Lists.newArrayList(
				//This is only for the development server to expose a GUI to use http://localhost:9000/@db
				new H2DbPlugin(),
				//if you want to use hibernate in production, move this to 
				//WEBPIECESxCLASSServer.java otherwise remove dependency on hibernate in build.gradle file
				//make sure you use a H2 persistence unit for dev and some other db persistence unit for production
				new HibernatePlugin("fortest") 
				);
		
		ServerConfig config = new ServerConfig();
		if(usePortZero) {
			config.setHttpPort(0);
			config.setHttpsPort(0);
		} else {
			//It is very important to run the dev server on different ports than the production server as if
			//a developer runs the production server locally, it will tell the browser to cache stuff and when
			//the developer goes back to development mode, they will notice updates to *.js and *.css files no
			//longer working anymore.  Instead, run production on :8080 and dev on :9000
			config.setHttpPort(9000);
			config.setHttpsPort(9443);
		}
		
		//It is very important to turn off caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		
		config.setMetaFile(metaFile);
		server = new WEBPIECESxCLASSServer(platformOverrides, null, plugins, config);
	}
	
	public static String modifyForIDE(String filePath1) {
		String directory = filePath1;
        //intellij and eclipse use different user directories... :( :(
        if(filePath1.contains("WEBPIECESxAPPNAME-dev")) {
            //eclipse starts in WEBPIECESxAPPNAME-dev so move one directory back
			//THIS works in BOTH webpieces/..../template and in the code generated for webapp projects
            directory = directory+"/..";
        } else if(filePath1.endsWith("webpieces")) {
        	//intellij is more annoying since it runs in webpieces for the template project we use to generate
			//AND THEN runs in the webapp directory which is way different path than the template directory
			directory = directory+"/webserver/webpiecesServerBuilder/templateProject";
		}
        
		return directory;
	}
	
	public void start() throws InterruptedException {
		server.start();		
	}

	public void stop() {
		server.stop();
	}
}
