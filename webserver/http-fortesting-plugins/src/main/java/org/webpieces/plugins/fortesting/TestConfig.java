package org.webpieces.plugins.fortesting;

import org.webpieces.util.file.VirtualFile;

import com.google.inject.Module;

public class TestConfig {

	private Module platformOverrides;
	private Module appOverrides;
	private boolean usePortZero = false;
	private VirtualFile metaFile;
	private boolean useTokenCheck = false;

	public TestConfig() {
	}
	
	public TestConfig(Module platformOverrides, Module appOverrides, boolean usePortZero, VirtualFile metaFile, boolean useTokenCheck) {
		this.platformOverrides = platformOverrides;
		this.appOverrides = appOverrides;
		this.usePortZero = usePortZero;
		this.metaFile = metaFile;
		this.useTokenCheck = useTokenCheck;
	}

	public Module getPlatformOverrides() {
		return platformOverrides;
	}

	public void setPlatformOverrides(Module platformOverrides) {
		this.platformOverrides = platformOverrides;
	}

	public Module getAppOverrides() {
		return appOverrides;
	}

	public void setAppOverrides(Module appOverrides) {
		this.appOverrides = appOverrides;
	}

	public boolean isUsePortZero() {
		return usePortZero;
	}

	public void setUsePortZero(boolean usePortZero) {
		this.usePortZero = usePortZero;
	}

	public VirtualFile getMetaFile() {
		return metaFile;
	}

	public void setMetaFile(VirtualFile metaFile) {
		this.metaFile = metaFile;
	}

	public boolean isUseTokenCheck() {
		return useTokenCheck;
	}

	public void setUseTokenCheck(boolean useTokenCheck) {
		this.useTokenCheck = useTokenCheck;
	}

}
