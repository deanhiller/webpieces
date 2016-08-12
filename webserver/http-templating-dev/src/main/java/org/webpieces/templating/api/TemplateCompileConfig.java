package org.webpieces.templating.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class TemplateCompileConfig {

	private Charset fileEncoding = StandardCharsets.UTF_8;
	//These two fields are used by the template compiling plugin...
	private boolean isPluginClient = false;
	private Set<String> customTagsFromPlugin = new HashSet<>();

	public TemplateCompileConfig() {
	}
	
	public TemplateCompileConfig(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public Charset getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public boolean isPluginClient() {
		return isPluginClient;
	}

	public void setPluginClient(boolean isPluginClient) {
		this.isPluginClient = isPluginClient;
	}

	public Set<String> getCustomTagsFromPlugin() {
		return customTagsFromPlugin;
	}

	public void setCustomTagsFromPlugin(Set<String> customTagsFromPlugin) {
		this.customTagsFromPlugin = customTagsFromPlugin;
	}

}
