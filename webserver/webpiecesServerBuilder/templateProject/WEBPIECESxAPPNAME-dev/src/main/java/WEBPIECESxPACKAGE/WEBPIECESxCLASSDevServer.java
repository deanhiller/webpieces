package WEBPIECESxPACKAGE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import WEBPIECESxPACKAGE.ServerConfig;
import WEBPIECESxPACKAGE.WEBPIECESxCLASSServer;

public class WEBPIECESxCLASSDevServer {

	private static final Logger log = LoggerFactory.getLogger(WEBPIECESxCLASSServer.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipe compiler, a classloading compilter jar(webpieces' runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compiler your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		new WEBPIECESxCLASSDevServer(false).start();
		
		synchronized(WEBPIECESxCLASSDevServer.class) {
			WEBPIECESxCLASSDevServer.class.wait();
		}
	}
	
	private WEBPIECESxCLASSServer server;

	public WEBPIECESxCLASSDevServer(boolean usePortZero) {
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
        String directory = modifyForIDE(filePath1);
        
		//list all source paths here as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(directory+"/WEBPIECESxAPPNAME/src/main/java"));
		
		VirtualFile metaFile = new VirtualFileImpl(directory + "/WEBPIECESxAPPNAME/src/main/resources/appmeta.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(WEBPIECESxCLASSServer.ALL_FILE_ENCODINGS);
		
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths)
										.setFileEncoding(WEBPIECESxCLASSServer.ALL_FILE_ENCODINGS);
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new DevTemplateModule(templateConfig));
		
		
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
		server = new WEBPIECESxCLASSServer(platformOverrides, null, config);
	}
	
	public static String modifyForIDE(String filePath1) {
		String directory = filePath1;
        //intellij and eclipse use different user directories... :( :(
        if(filePath1.contains("WEBPIECESxAPPNAME-dev")) {
            //eclipse starts in WEBPIECESxAPPNAME-dev so move one directory back
            directory = directory+"/..";
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
