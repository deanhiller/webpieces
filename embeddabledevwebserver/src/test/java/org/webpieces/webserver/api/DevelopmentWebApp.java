package org.webpieces.webserver.api;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevModule;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

import com.google.inject.Module;

public class DevelopmentWebApp {
	
	public static void main(String[] args) throws InterruptedException {
		String filePath1 = System.getProperty("user.dir");
		VirtualFile srcDir = new VirtualFileImpl(filePath1+"/../embeddablewebserver/src/test/java");
		CompileConfig devConfig = new CompileConfig(srcDir);
		Module platformOverrides = new DevModule(devConfig);
		
		new MyWebApp(platformOverrides, null).start();
	}
	
}
