package org.webpieces.compiler.api;

import org.webpieces.compiler.impl.CompileOnDemandImpl;

public class CompileOnDemandFactory {

	public static CompileOnDemand createCompileOnDemand(CompileConfig config) {
		return new CompileOnDemandImpl(config);
	}
	
	public static CompileOnDemand createCompileOnDemand(CompileConfig config, String basePackage) {
		return new CompileOnDemandImpl(config, basePackage);
	}	
}
