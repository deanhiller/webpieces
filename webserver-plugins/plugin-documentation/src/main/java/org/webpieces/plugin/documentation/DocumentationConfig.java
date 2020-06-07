package org.webpieces.plugin.documentation;

public class DocumentationConfig {

	private String pluginPath = "/@documentation";

	public DocumentationConfig() {
	}
	
	public DocumentationConfig(String pluginPath) {
		super();
		this.pluginPath = pluginPath;
	}

	public String getPluginPath() {
		return pluginPath;
	}

}
