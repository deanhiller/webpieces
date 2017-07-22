package org.webpieces.util.file;

import java.io.File;

public class VirtualFileFactory {
	
	public static VirtualFile newFile(File baseDir, String relativePath) {
		File file = FileFactory.newFile(baseDir, relativePath);
		return new VirtualFileImpl(file);
	}
	
	public static VirtualFile newFile(File absoluteFile) {
		if(!absoluteFile.isAbsolute())
			throw new IllegalArgumentException("file must be absolute path created");
		return new VirtualFileImpl(absoluteFile);
	}

	public static VirtualFile newBaseFile(String absPath) {
		File file = FileFactory.newBaseFile(absPath);
		return new VirtualFileImpl(file);
	}

	public static VirtualFileImpl newAbsoluteFile(String path) {
		File file = FileFactory.newAbsoluteFile(path);
		return new VirtualFileImpl(file);
	}
	
}
