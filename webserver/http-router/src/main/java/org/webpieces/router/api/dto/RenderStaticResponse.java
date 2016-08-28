package org.webpieces.router.api.dto;

public class RenderStaticResponse {

	private boolean isOnClassPath;
	private String absolutePath;
	public RenderStaticResponse(String absolutePath, boolean isOnClassPath) {
		this.isOnClassPath = isOnClassPath;
		this.absolutePath = absolutePath;
	}
	public boolean isOnClassPath() {
		return isOnClassPath;
	}
	public String getAbsolutePath() {
		return absolutePath;
	}
	
}
