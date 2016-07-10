package org.webpieces.templating.impl;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.impl.source.ScriptCode;

import groovy.lang.GroovyClassLoader;

public class GroovyCompile {

	public Class<?> compile(ScriptCode scriptCode) {
		try {
			return compileImpl(scriptCode);
			//F'ing checked exceptions should have been runtime so I don't have all this cruft in my app...
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> compileImpl(ScriptCode scriptCode) throws ClassNotFoundException {
		GroovyClassLoader cl = new GroovyClassLoader();
		CompilationUnit compileUnit = new CompilationUnit();
	    compileUnit.addSource(scriptCode.getFullClassName(), scriptCode.getScriptSourceCode());
	    compileUnit.compile(Phases.CLASS_GENERATION);
	    compileUnit.setClassLoader(cl);

	    GroovyClass target = null;
	    for (Object compileClass : compileUnit.getClasses()) {
	        GroovyClass groovyClass = (GroovyClass) compileClass;
	        cl.defineClass(groovyClass.getName(), groovyClass.getBytes());
	        if(groovyClass.getName().equals(scriptCode.getFullClassName())) {
	        	target = groovyClass;
	        }
	    }

	    if(target == null) 
	    	throw new IllegalStateException("Could not find proper class");
	    
	    return cl.loadClass(target.getName());
	}

}
