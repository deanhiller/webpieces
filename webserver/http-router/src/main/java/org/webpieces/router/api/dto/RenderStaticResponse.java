package org.webpieces.router.api.dto;

import java.io.File;

import org.webpieces.util.file.VirtualFile;

public class RenderStaticResponse {

	private File targetCache;
	private boolean isOnClassPath;
	
	private VirtualFile filePath;
	private String relativeUrl;

	public RenderStaticResponse(File targetCache, boolean isOnClassPath) {
		this.targetCache = targetCache;
		this.isOnClassPath = isOnClassPath;
	}
	
	public boolean isOnClassPath() {
		return isOnClassPath;
	}

	public VirtualFile getFilePath() {
		return filePath;
	}

	public void setFilePath(VirtualFile filePath) {
		this.filePath = filePath;
	}

	public File getTargetCache() {
		return targetCache;
	}

	public void setFileAndRelativePath(VirtualFile fullPath, String relativeUrl) {
		this.filePath = fullPath;
		this.relativeUrl = relativeUrl;
	}

	public String getRelativeUrl() {
		return relativeUrl;
	}
	
}
