package webpiecesxxxxxpackage.meta;

import java.io.File;

import org.webpieces.util.file.FileFactory;

public class JavaCache {

	public static File getCacheLocation() {
		return FileFactory.newCacheLocation("WEBPIECESxAPPNAMECache/precompressedFiles");
	}
	
}
