package org.webpieces.templating.api;

import java.util.List;

import org.codehaus.groovy.tools.GroovyClass;

import groovy.lang.GroovyClassLoader;

public interface CompileCallback {

	void compiledGroovyClass(GroovyClassLoader groovyCl, GroovyClass clazz);
	
	/**
	 * Allows the compiler to write a file out with all routeids from html files that can be used at startup time
	 * to validate all routes so we catch any errors on mistyped route ids before going to production (ie. build time as your
	 * tests should be run then)
	 * 
	 * @param routeId
	 * @param argNames
	 * @param sourceLocation
	 */
	void recordRouteId(String routeId, List<String> argNames, String sourceLocation);

	void recordPath(String relativeUrlPath, String sourceLocation);
}
