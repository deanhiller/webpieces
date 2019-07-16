package org.webpieces.webserver;

import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;

import com.google.inject.Module;

public class PrivateTestConfig {

	private Module platformOverrides;
	private Module appOverrides;
	private VirtualFile metaFile = new VirtualFileClasspath("basicMeta.txt", PrivateWebserverForTest.class.getClassLoader());
	private boolean useTokenCheck = false;

	public PrivateTestConfig() {
	}
	
	public PrivateTestConfig(Module platformOverrides, Module appOverrides, VirtualFile metaFile, boolean useTokenCheck) {
		this.platformOverrides = platformOverrides;
		this.appOverrides = appOverrides;
		if(metaFile == null)
			this.metaFile = new VirtualFileClasspath("basicMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		else
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
