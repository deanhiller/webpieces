package org.webpieces.compiler.api;

import org.webpieces.compiler.impl.CompileOnDemandImpl;

public class CompileOnDemandFactory {

	public CompileOnDemand createCompileOnDemand(CompileConfig config) {
		return new CompileOnDemandImpl(config);
	}
	
	public CompileOnDemand createCompileOnDemand(CompileConfig config, String basePackage) {
		return new CompileOnDemandImpl(config, basePackage);
	}	
}
