package WEBPIECESxPACKAGE;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileFactory;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Module;

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
		
		VirtualFileImpl directory = modifyForIDE();
        
		//list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(directory.child("WEBPIECESxAPPNAME/src/main/java"));
		srcPaths.add(directory.child("WEBPIECESxAPPNAME-dev/src/main/java"));
		
//		VirtualFile metaFile = new VirtualFileImpl(directory + "/WEBPIECESxAPPNAME/src/main/resources/appmetadev.txt");
//		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths)
														.setFileEncoding(Server.ALL_FILE_ENCODINGS);
		
		Module platformOverrides = new DevTemplateModule(templateConfig);
		
		ServerConfig config = new ServerConfig(HibernatePlugin.PERSISTENCE_TEST_UNIT);
		
		//It is very important to turn off caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		//config.setMetaFile(metaFile);
		
		server = new Server(platformOverrides, null, config);
	}
	
	public static VirtualFileImpl modifyForIDE() {
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
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
        
		return VirtualFileFactory.newAbsoluteFile(directory);
	}
	
	public void start() throws InterruptedException {
		server.start();		
	}

	public void stop() {
		server.stop();
	}
}
