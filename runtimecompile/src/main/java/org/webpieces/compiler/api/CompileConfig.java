package org.webpieces.compiler.api;

import java.util.List;

import org.webpieces.util.file.VirtualFile;

public class CompileConfig {

	private List<VirtualFile> javaPath;
	private VirtualFile byteCodeCacheDir;

	/**
	 * VirtualFiles can be created with new VirtualFileImpl
	 * 
	 * @param javaPath The list of paths we will compile code from
	 * @param byteCodeCacheDir The directory we use to cache class byte code so we can avoid recompiling in certain cases
	 */
	public CompileConfig(List<VirtualFile> javaPath, VirtualFile byteCodeCacheDir) {
		this.javaPath = javaPath;
		this.byteCodeCacheDir = byteCodeCacheDir;
	}

	public List<VirtualFile> getJavaPath() {
		return javaPath;
	}

	public VirtualFile getByteCodeCacheDir() {
		return byteCodeCacheDir;
	}

}
