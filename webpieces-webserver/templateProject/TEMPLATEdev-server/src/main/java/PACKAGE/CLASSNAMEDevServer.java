package PACKAGE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class CLASSNAMEDevServer {

	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipe compiler, a classloading compilter jar(webpieces' runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compiler your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		new CLASSNAMEDevServer(false).start();
		
		synchronized(CLASSNAMEDevServer.class) {
			CLASSNAMEDevServer.class.wait();
		}
	}
	
	private CLASSNAMEServer server;

	public CLASSNAMEDevServer(boolean usePortZero) {
		String filePath1 = System.getProperty("user.dir");
		
		//list all source paths here as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(filePath1+"/../TEMPLATEAPPNAME/src/main/java"));
		
		CompileConfig devConfig = new CompileConfig(srcPaths);
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new DevTemplateModule());
		server = new CLASSNAMEServer(platformOverrides, null, usePortZero);
	}
	
	public void start() throws InterruptedException {
		server.start();		
	}

	public void stop() {
		server.stop();
	}
}