package org.webpieces.router.api;

import org.webpieces.util.file.VirtualFile;

import com.google.inject.Module;

public class HttpRouterConfig {

	private VirtualFile routersFile;
	
	/**
	 * WebApps can override remote services to mock them out for testing or swap prod classes with
	 * an in-memory implementation such that tests can remain single threaded
	 */
	private Module webappOverrides;
	
	public VirtualFile getRoutersFile() {
		return routersFile;
	}
	public Module getOverridesModule() {
		return webappOverrides;
	}
	public HttpRouterConfig setRoutersFile(VirtualFile routersFile) {
		if(!routersFile.exists())
			throw new IllegalArgumentException("path="+routersFile+" does not exist");
		else if(routersFile.isDirectory())
			throw new IllegalArgumentException("path="+routersFile+" is a directory and needs to be a file");
		this.routersFile = routersFile;
		return this;
	}
	public HttpRouterConfig setWebappOverrides(Module webappOverrides) {
		this.webappOverrides = webappOverrides;
		return this;
	}

}
