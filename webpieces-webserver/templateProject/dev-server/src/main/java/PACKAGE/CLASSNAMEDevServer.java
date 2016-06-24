package PACKAGE;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevModule;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Module;

public class CLASSNAMEDevServer {

	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipe compiler, a classloading compilter jar(webpieces' runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compiler your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		String filePath1 = System.getProperty("user.dir");
		
		//list all source paths here as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(new VirtualFileImpl(filePath1+"/../ZAPPNAME/src/main/java"));
		
		CompileConfig devConfig = new CompileConfig(srcPaths);
		Module platformOverrides = new DevModule(devConfig);
		new CLASSNAMEServer(platformOverrides, null).start();
		
		synchronized(CLASSNAMEDevServer.class) {
			CLASSNAMEDevServer.class.wait();
		}
	}
}