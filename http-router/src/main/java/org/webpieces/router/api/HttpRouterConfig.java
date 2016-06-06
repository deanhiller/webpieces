package org.webpieces.router.api;

import org.webpieces.util.file.VirtualFile;

import com.google.inject.Module;

public class HttpRouterConfig {

	private VirtualFile routersFile;
	private Module overridesModule;
	
	public HttpRouterConfig(VirtualFile routersFile, Module overridesModule) {
		super();
		this.routersFile = routersFile;
		this.overridesModule = overridesModule;
	}
	public VirtualFile getRoutersFile() {
		return routersFile;
	}
	public Module getOverridesModule() {
		return overridesModule;
	}
	
}
