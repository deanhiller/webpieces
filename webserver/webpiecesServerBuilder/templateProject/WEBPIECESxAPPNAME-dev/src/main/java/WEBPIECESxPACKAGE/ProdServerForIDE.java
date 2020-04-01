package WEBPIECESxPACKAGE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.api.IDESupport;
import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Uses the production Router but the dev template compiler so you 'could' step through prod router code
 * to see if something is going on
 * 
 * TODO: modify this to compile ALL gradle groovy template *.class files (ones generated from the html) and then we
 * could run full production mode from the IDE
 */
public class ProdServerForIDE {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		log.info("Starting Server");
		new ProdServerForIDE(false).start();
		
		//Since we typically use only 3rd party libraries with daemon threads, that means this
		//main thread is the ONLY non-daemon thread letting the server keep running so we need
		//to block it and hold it up from exiting.  Modify this to release if you want an ability
		//to remotely shutdown....
		synchronized(ProdServerForIDE.class) {
			ProdServerForIDE.class.wait();
		}
	}
	
	private Server server;

	public ProdServerForIDE(boolean usePortZero) {
		
		String name = "WEBPIECESxAPPNAME";
		VirtualFileImpl directory = IDESupport.modifyForIDE(name);
        
		//list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(directory.child(name+"/src/main/java"));
		srcPaths.add(directory.child(name+"-dev/src/main/java"));
		
//		VirtualFile metaFile = new VirtualFileImpl(directory + "/WEBPIECESxAPPNAME/src/main/resources/appmetadev.txt");
//		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths)
														.setFileEncoding(Server.ALL_FILE_ENCODINGS);
		
		Module platformOverrides = new DevTemplateModule(templateConfig);
		
		ServerConfig config = new ServerConfig(false);
		
		//It is very important to turn off caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		//config.setMetaFile(metaFile);
		
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		Module all = Modules.combine(platformOverrides, new SimpleMeterModule(metrics));
		server = new Server(all, null, config, "-hibernate.persistenceunit=hibernatefortest");
	}
	

	
	public void start() throws InterruptedException {
		server.start();		
	}

}
