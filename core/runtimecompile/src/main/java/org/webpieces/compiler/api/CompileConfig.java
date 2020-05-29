package org.webpieces.compiler.api;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFileImpl;

public class CompileConfig {

	private List<VirtualFile> javaPath;
	private VirtualFile byteCodeCacheDir;
	private Charset fileEncoding = StandardCharsets.UTF_8;
	
	private String failIfNotInSourcePaths = null;
	
	/**
	 * VirtualFiles can be created with new VirtualFileImpl
	 * 
	 * @param javaPath The list of paths we will compile code from
	 * @param byteCodeCacheDir The directory we use to cache class byte code so we can avoid recompiling in certain cases
	 */
	public CompileConfig(List<VirtualFile> javaPath, VirtualFile byteCodeCacheDir) {
		for(VirtualFile f : javaPath) {
			if(!f.exists())
				throw new IllegalArgumentException("Directory="+f.getCanonicalPath()+" is not found");
			else if(!f.isDirectory())
				throw new IllegalArgumentException("Only directories are allowed.  This file="+f.getCanonicalPath()+" is a file not a directory");
		}
		
		this.javaPath = javaPath;
		this.byteCodeCacheDir = byteCodeCacheDir;
	}
	
	public CompileConfig(VirtualFile javaPath, VirtualFile byteCodeCacheDir) {
		this(createList(javaPath), byteCodeCacheDir);
	}

	public CompileConfig(List<VirtualFile> javaPaths) {
		this(javaPaths, new VirtualFileImpl(
				FileFactory.newBaseFile("webpiecesCache/bytecodeCache")
				));
	}
	
	public static VirtualFile getHomeCacheDir(String relativePath) {
		File cacheDir = FileFactory.newCacheLocation(relativePath);
		return new VirtualFileImpl(cacheDir);
	}

	private static List<VirtualFile> createList(VirtualFile javaPath) {
		if(!javaPath.exists())
			throw new IllegalArgumentException("path="+javaPath+" does not exist");
		else if(!javaPath.isDirectory())
			throw new IllegalArgumentException("path="+javaPath+" is not a directory");
		
		List<VirtualFile> list = new ArrayList<>();
		list.add(javaPath);
		return list;
	}
	
	public List<VirtualFile> getJavaPath() {
		return javaPath;
	}

	public VirtualFile getByteCodeCacheDir() {
		return byteCodeCacheDir;
	}

	public Charset getFileEncoding() {
		return fileEncoding ;
	}

	public CompileConfig setFileEncoding(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
		return this;
	}

	public String getFailIfNotInSourcePaths() {
		return failIfNotInSourcePaths;
	}

	public CompileConfig setFailIfNotInSourcePaths(String failIfNotInSourcePaths) {
		this.failIfNotInSourcePaths = failIfNotInSourcePaths;
		return this;
	}

}
