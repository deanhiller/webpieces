package org.webpieces.compiler.impl;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemand;

public class CompileOnDemandImpl implements CompileOnDemand {

	private final CompileConfig config;
	private final CompileMetaMgr appClassMgr;
	private final FileLookup fileLookup;	
	private final CompilerWrapper compiler;
	
	public CompilingClassloader classloader;

	public CompileOnDemandImpl(CompileConfig config) {
		this(config, "");
	}
	
	public CompileOnDemandImpl(CompileConfig config, String basePackage) {
		this.config = config;
		appClassMgr = new CompileMetaMgr(config);
		fileLookup = new FileLookup(appClassMgr, config.getJavaPath());
		compiler = new CompilerWrapper(appClassMgr, fileLookup, config);
		classloader = new CompilingClassloader(config, compiler, fileLookup);
		fileLookup.scanFilesWithFilter(basePackage);
	}
	
	@Override
	public Class<?> loadClass(String name) {
		if(classloader.isNeedToReloadJavaFiles()) {
			classloader = new CompilingClassloader(config, compiler, fileLookup);
		}
		Class<?> clazz = classloader.loadApplicationClass(name);
		if(clazz == null) {
			clazz = loadClassFromDefaultClassloader(name);
		}
		
		if(clazz == null)
			throw new IllegalArgumentException("class name="+name+" is not found in the source directories="+config.getJavaPath()+" nor on the classpath");
		return clazz;
	}

	private Class<?> loadClassFromDefaultClassloader(String name) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			return cl.loadClass(name);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("class name="+name+" is not found in the source directories="+config.getJavaPath()+" nor on the classpath");
		}
	}

	@Override
	public Class<?> loadClass(String name, boolean forceReload) {
		if(forceReload) {
			classloader = new CompilingClassloader(config, compiler, fileLookup);
		}
		return loadClass(name);
	}
}
