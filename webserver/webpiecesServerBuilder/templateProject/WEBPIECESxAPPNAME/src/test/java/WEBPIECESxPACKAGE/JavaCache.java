package WEBPIECESxPACKAGE;

import java.io.File;

public class JavaCache {

	public static File getCacheLocation() {
		String tmpPath = System.getProperty("java.io.tmpdir");
		return new File(tmpPath+"/webpieces/WEBPIECESxAPPNAMECache/staticFiles");
	}
	
}
