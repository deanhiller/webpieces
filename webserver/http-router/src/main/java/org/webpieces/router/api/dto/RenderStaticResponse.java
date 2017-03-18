package org.webpieces.router.api.dto;

import java.io.File;

public class RenderStaticResponse {

	private File targetCache;
	private boolean isOnClassPath;
	//This is one or the other....(need an Either in java)
	private String filePath;
	private String directory;
	private String relativePath;

	public RenderStaticResponse(File targetCache, boolean isOnClassPath) {
		this.targetCache = targetCache;
		this.isOnClassPath = isOnClassPath;
	}
	
	public boolean isOnClassPath() {
		return isOnClassPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDirectory() {
		return directory;
	}

	public void setRelativeFile(String directory, String relativePath) {
		this.directory = directory;
		this.relativePath = relativePath;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public File getTargetCache() {
		return targetCache;
	}
	
}
