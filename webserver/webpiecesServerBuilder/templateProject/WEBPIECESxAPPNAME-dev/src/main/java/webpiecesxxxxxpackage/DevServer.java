package webpiecesxxxxxpackage;

import java.util.ArrayList;

import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.webserver.api.IDESupport;

public abstract class DevServer {

	protected VirtualFileImpl directory;
	protected ArrayList<VirtualFile> srcPaths;
	protected String[] args;

	public DevServer(boolean usePortZero) {
		String name = "WEBPIECESxAPPNAME";
		directory = IDESupport.modifyForIDE(name);
		
		//list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
		srcPaths = new ArrayList<>();
		srcPaths.add(directory.child(name+"/src/main/java"));
		srcPaths.add(directory.child(name+"-dev/src/main/java"));
		
		if(usePortZero)
			args = new String[] {
					"-https.over.http=true",
					"-http.port=:0",
					"-https.port=:0",
					"-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory"
					};
		else
			args = new String[] {
					"-https.over.http=true", //in DevelopmentServer, we allow https into the http port(and http).  In production, it's your choice
					"-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory"
					};
	}
	
	public abstract void start(); 
	

}
