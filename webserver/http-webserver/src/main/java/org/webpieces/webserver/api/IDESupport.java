package org.webpieces.webserver.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.file.VirtualFileFactory;
import org.webpieces.util.file.VirtualFileImpl;

public class IDESupport {

	private static final Logger log = LoggerFactory.getLogger(WebpiecesServer.class);
	private static String filePathForWebpiecesDevelopers;
    public static VirtualFileImpl modifyForIDE(String name) {
		return modifyForIDE(name, null);
	}
	public static VirtualFileImpl modifyForIDE(String name, String projectIfDebugInWebpieces) {
		String filePath1 = System.getProperty("user.dir");
		log.info("running from dir="+filePath1);
		
		String directory = filePath1;
        //intellij and eclipse use different user directories... :( :(
        if(filePath1.contains(name+"-dev") || filePath1.contains("development")) {
            //eclipse starts in WEBPIECESxAPPNAME-dev so move one directory back
			//THIS works in BOTH webpieces/..../template and in the code generated for webapp projects
            directory = directory+"/..";
        } else if(filePath1.endsWith("webpieces")) {
			if(projectIfDebugInWebpieces == null)
				throw new IllegalArgumentException("Must pass in project name to run out of webpieces git repository");
        	//intellij is more annoying since it runs in webpieces for the template project we use to generate
			//AND THEN runs in the webapp directory which is way different path than the template directory
			directory = directory+"/webserver-templates/webpiecesServerBuilder/"+projectIfDebugInWebpieces;
			filePathForWebpiecesDevelopers = directory;
		}
        
		return VirtualFileFactory.newAbsoluteFile(directory);
	}

	public static String fetchPath() {
		return filePathForWebpiecesDevelopers;
	}
}
