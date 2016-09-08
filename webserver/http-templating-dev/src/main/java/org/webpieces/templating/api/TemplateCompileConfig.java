package org.webpieces.templating.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.webpieces.util.file.VirtualFile;

public class TemplateCompileConfig {

	private Charset fileEncoding = StandardCharsets.UTF_8;
	//These two fields are used by the template compiling plugin...
	private boolean isPluginClient = false;
	private Set<String> customTagsFromPlugin = new HashSet<>();
	private List<VirtualFile> srcPaths = new ArrayList<>();

	public TemplateCompileConfig() {
	}
	
	public TemplateCompileConfig(List<VirtualFile> srcPaths) {
		this.srcPaths = srcPaths;
	}

	public Charset getFileEncoding() {
		return fileEncoding;
	}

	public TemplateCompileConfig setFileEncoding(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
		return this;
	}

	public boolean isPluginClient() {
		return isPluginClient;
	}

	public TemplateCompileConfig setPluginClient(boolean isPluginClient) {
		this.isPluginClient = isPluginClient;
		return this;
	}

	public Set<String> getCustomTagsFromPlugin() {
		return customTagsFromPlugin;
	}

	public TemplateCompileConfig setCustomTagsFromPlugin(Set<String> customTagsFromPlugin) {
		this.customTagsFromPlugin = customTagsFromPlugin;
		return this;
	}
	
	public List<VirtualFile> getSrcPaths() {
		return srcPaths;
	}

	public TemplateCompileConfig setSrcPaths(List<VirtualFile> srcPaths) {
		this.srcPaths = srcPaths;
		return this;
	}
}
