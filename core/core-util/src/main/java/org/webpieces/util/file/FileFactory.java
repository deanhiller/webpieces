package org.webpieces.util.file;

import java.io.File;

public class FileFactory {

	/**
	 * Unlike user.dir property, we can override this for the whole JVM now to make things work
	 * see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4117557
	 * and https://stackoverflow.com/questions/45130661/user-dir-property-broken-on-osx-jdk-1-8-0-111-how-about-other-os-versions
	 */
	public static File getBaseWorkingDir() {
		String property = System.getProperty("user.dir");
		return new File(property);
	}
	
	public static File getTmpDirectory() {
		String tmpPath = System.getProperty("java.io.tmpdir");
		return new File(tmpPath);
	}
	
	public static File newAbsoluteFile(String absolutePath) {
		File f = new File(absolutePath);
		if(!f.isAbsolute())
			throw new IllegalArgumentException("The file path must be absolute and was not.  path="+absolutePath);
		return f;
	}
	
	public static File newFile(File baseDir, String relativePath) {
		if(!baseDir.isAbsolute())
			throw new IllegalArgumentException("baseDir MUST be absolute to avoid dependencies on user.dir property which is not modifiable while working dir is");
			
		String correctPathForThisOs = convertToOsPath(relativePath);
		return new File(baseDir, correctPathForThisOs);
	}
	
	public static File newBaseFile(String relativePath) {
		File workingDir = getBaseWorkingDir();
		
		String correctPathForThisOs = convertToOsPath(relativePath);
		
		return new File(workingDir, correctPathForThisOs );
	}

	public static File newTmpFile(String relativePath) {
		File tmpDirectory = getTmpDirectory();
		String correctPathForThisOs = convertToOsPath(relativePath);
		return new File(tmpDirectory, correctPathForThisOs);
	}
	
	static String convertToOsPath(String relativePath) {
		if(relativePath.startsWith("/"))
			throw new IllegalArgumentException("Relative path cannot start with /. path="+relativePath);

		String[] split = relativePath.split("/|\\\\");
		StringBuilder builder = new StringBuilder(split[0]);
		for(int i = 1; i < split.length; i++) {
			builder.append(File.separator+split[i]);
		}
		String correctPathForThisOs = builder.toString();
		return correctPathForThisOs;
	}

	public static boolean endsWith(File filePath, String path) {
		String osPath = convertToOsPath(path);
		return filePath.getPath().endsWith(osPath);
	}
}
