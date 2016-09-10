package org.webpieces.router.api.dto;

public class RenderStaticResponse {

	private String staticRouteId;
	private boolean isOnClassPath;
	//This is one or the other....(need an Either in java)
	private String filePath;
	private String directory;
	private String relativePath;

	public RenderStaticResponse(String staticRouteId, boolean isOnClassPath) {
		this.staticRouteId = staticRouteId;
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

	public String getStaticRouteId() {
		return staticRouteId;
	}
	
}
