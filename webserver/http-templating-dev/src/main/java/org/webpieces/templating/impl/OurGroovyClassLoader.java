package org.webpieces.templating.impl;

import java.util.HashSet;
import java.util.Set;

import groovy.lang.GroovyClassLoader;

public class OurGroovyClassLoader extends GroovyClassLoader {

	private Set<String> definedClasses = new HashSet<>();
	
	@SuppressWarnings("rawtypes")
	@Override
    public Class defineClass(String name, byte[] b) {
    	definedClasses.add(name);
        return super.defineClass(name, b, 0, b.length);
    }
	
	public boolean isClassDefined(String name) {
		return definedClasses.contains(name);
	}
}
