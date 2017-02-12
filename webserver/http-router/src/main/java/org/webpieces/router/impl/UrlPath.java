package org.webpieces.router.impl;

import org.webpieces.router.impl.model.RouterInfo;

public class UrlPath {

	private String prefix;
	private String subPath;

	public UrlPath(RouterInfo info, String subPath) {
		this.prefix = info.getPath();
		this.subPath = subPath;
	}
	
	public UrlPath(String prefix, String subPath) {
		this.prefix = prefix;
		this.subPath = subPath;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSubPath() {
		return subPath;
	}

	public String getFullPath() {
		return prefix+subPath;
	}
	
}
