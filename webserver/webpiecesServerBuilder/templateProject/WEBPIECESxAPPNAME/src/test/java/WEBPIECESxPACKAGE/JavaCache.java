package WEBPIECESxPACKAGE;

import java.io.File;

import org.webpieces.util.file.FileFactory;

public class JavaCache {

	public static File getCacheLocation() {
		return FileFactory.newTmpFile("webpieces/WEBPIECESxAPPNAMECache/precompressedFiles");
	}
	
}
