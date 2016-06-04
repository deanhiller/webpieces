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
		appClassMgr = new CompileMetaMgr();
		fileLookup = new FileLookup(appClassMgr, config.getJavaPath());
		compiler = new CompilerWrapper(appClassMgr, fileLookup);
		classloader = new CompilingClassloader(config, compiler);
		fileLookup.scanFilesWithFilter(basePackage);
	}
	
	public Class<?> loadClass(String name) {
		if(classloader.isNeedToReloadJavaFiles()) {
			classloader = new CompilingClassloader(config, compiler);
		}
		return classloader.loadApplicationClass(name);
	}

}
