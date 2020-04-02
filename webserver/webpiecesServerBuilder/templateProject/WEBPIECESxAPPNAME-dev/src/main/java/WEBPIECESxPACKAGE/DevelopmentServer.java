package WEBPIECESxPACKAGE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.api.IDESupport;
import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class DevelopmentServer {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		try {
			String version = System.getProperty("java.version");
			log.info("Starting Development Server under java version="+version);

			new DevelopmentServer(false).start();
			
			//Since we typically use only 3rd party libraries with daemon threads, that means this
			//main thread is the ONLY non-daemon thread letting the server keep running so we need
			//to block it and hold it up from exiting.  Modify this to release if you want an ability
			//to remotely shutdown....
			synchronized(DevelopmentServer.class) {
				DevelopmentServer.class.wait();
			}
		} catch(Throwable e) {
			log.error("Failed to startup.  exiting jvm. msg="+e.getMessage(), e);
			System.exit(1); // should not be needed BUT some 3rd party libraries start non-daemon threads :(
		}
	}
	
	private Server server;

	public DevelopmentServer(boolean usePortZero) {
		
		String name = "WEBPIECESxAPPNAME";
		VirtualFileImpl directory = IDESupport.modifyForIDE(name);
        
		//list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(directory.child(name+"/src/main/java"));
		srcPaths.add(directory.child(name+"-dev/src/main/java"));
		
		VirtualFile metaFile = directory.child("WEBPIECESxAPPNAME/src/main/resources/appmetadev.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());

		SimpleMeterRegistry metrics = new SimpleMeterRegistry();

		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths)
														.setFileEncoding(Server.ALL_FILE_ENCODINGS);
		
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths, CompileConfig.getHomeCacheDir("WEBPIECESxAPPNAMECache/devserver-bytecode"))
										.setFileEncoding(Server.ALL_FILE_ENCODINGS);
		Module platformOverrides = Modules.combine(
										new SimpleMeterModule(metrics),
										new DevRouterModule(devConfig),
										new DevTemplateModule(templateConfig));
		
		String[] args;
		if(usePortZero)
			args = new String[] {"-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=hibernatefortest"};
		else
			args = new String[] {"-hibernate.persistenceunit=hibernatefortest"};
		
		ServerConfig config = new ServerConfig(false);

		//READ the documentation in HttpSvrInstanceConfig for more about these settings
//		HttpSvrInstanceConfig backendSvrConfig = new HttpSvrInstanceConfig(new InetSocketAddress(8444), sslFactory);
//		backendSvrConfig.setFunctionToConfigureServerSocket((s) -> Server.configure(s));
//		config.setBackendSvrConfig(backendSvrConfig );
		
		//It is very important to turn off BROWSER caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		config.setMetaFile(metaFile);
		
		server = new Server(platformOverrides, null, config, args);
	}
	
	public void start() throws InterruptedException {
		server.start();		
	}

	public void stop() {
		server.stop();
	}

}
