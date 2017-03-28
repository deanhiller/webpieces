package org.webpieces.templating.impl;

import java.util.List;

import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.api.CompileCallback;

import groovy.lang.GroovyClassLoader;

public class DevTemplateCompileCallback implements CompileCallback {

	@Override
	public void compiledGroovyClass(GroovyClassLoader cl, GroovyClass groovyClass) {
		cl.defineClass(groovyClass.getName(), groovyClass.getBytes());	
	}
	
	@Override
	public void recordRouteId(String routeId, List<String> argNames, String sourceLocation) {
	}

	@Override
	public void recordPath(String relativeUrlPath, String sourceLocation) {
	}
	
}