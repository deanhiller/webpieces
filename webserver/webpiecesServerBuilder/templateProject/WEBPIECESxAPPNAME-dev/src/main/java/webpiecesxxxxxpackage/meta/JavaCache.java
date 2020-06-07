package webpiecesxxxxxpackage.meta;

import java.io.File;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;

public class JavaCache {

	public static File getCacheLocation() {
		return FileFactory.newCacheLocation("WEBPIECESxAPPNAMECache/precompressedFiles");
	}

	public static VirtualFile getByteCache() {
		return CompileConfig.getHomeCacheDir("WEBPIECESxAPPNAMECache/devserver-bytecode");
	}
}
