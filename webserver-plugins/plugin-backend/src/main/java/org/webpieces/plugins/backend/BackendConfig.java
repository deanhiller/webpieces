package org.webpieces.plugins.backend;

public class BackendConfig {

	private boolean isUsePluginAssets;

	public BackendConfig(boolean isUsePluginAssets) {
		this.isUsePluginAssets = isUsePluginAssets;
	}
	
	public BackendConfig() {
	}

	public boolean isUsePluginAssets() {
		return isUsePluginAssets;
	}
	
}
