package org.webpieces.router.impl.services;

import java.io.File;

import org.webpieces.util.file.VirtualFile;

public class RouteInfoForStatic {

	private final boolean isOnClassPath;
	private final File targetCacheLocation;
	private final VirtualFile fileSystemPath;
	
	public RouteInfoForStatic(boolean isOnClassPath, File targetCacheLocation, VirtualFile fileSystemPath) {
		super();
		this.isOnClassPath = isOnClassPath;
		this.targetCacheLocation = targetCacheLocation;
		this.fileSystemPath = fileSystemPath;
	}

	public boolean isOnClassPath() {
		return isOnClassPath;
	}

	public File getTargetCacheLocation() {
		return targetCacheLocation;
	}

	public VirtualFile getFileSystemPath() {
		return fileSystemPath;
	}

}
