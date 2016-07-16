package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.router.impl.hooks.ClassForName;

public class DevClassForName implements ClassForName {

	private CompileOnDemand compileOnDemand;

	@Inject
	public DevClassForName(CompileOnDemand compile) {
		this.compileOnDemand = compile;
	}
	
	@Override
	public Class<?> clazzForName(String moduleName) {
		return compileOnDemand.loadClass(moduleName);
	}

}
