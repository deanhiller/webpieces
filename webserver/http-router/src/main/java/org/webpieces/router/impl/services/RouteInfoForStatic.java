package org.webpieces.router.impl.services;

import java.io.File;

import org.webpieces.util.file.VirtualFile;

public class RouteInfoForStatic {

	private final boolean isOnClassPath;
	private final File targetCacheLocation;
	private final VirtualFile fileSystemPath;
	private final boolean isRouteAFile;

	public RouteInfoForStatic(boolean isOnClassPath, File targetCacheLocation, VirtualFile fileSystemPath, boolean isRouteAFile) {
		super();
		this.isOnClassPath = isOnClassPath;
		this.targetCacheLocation = targetCacheLocation;
		this.fileSystemPath = fileSystemPath;
		this.isRouteAFile = isRouteAFile;
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

	public boolean isRouteAFile() {
		return isRouteAFile;
	}
}
