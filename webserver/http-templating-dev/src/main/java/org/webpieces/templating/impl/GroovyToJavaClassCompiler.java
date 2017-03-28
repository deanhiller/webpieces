package org.webpieces.templating.impl;

import javax.inject.Inject;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.impl.source.ScriptOutputImpl;

import groovy.lang.GroovyClassLoader;

public class GroovyToJavaClassCompiler {

	private CompileCallback callbacks;

	@Inject
	public GroovyToJavaClassCompiler(CompileCallback callbacks) {
		this.callbacks = callbacks;
	}
	
	public void compile(GroovyClassLoader cl, ScriptOutputImpl scriptCode) {
		try {
			compileImpl(cl, scriptCode);
			//F'ing checked exceptions should have been runtime so I don't have all this cruft in my app...
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	private void compileImpl(GroovyClassLoader groovyCl, ScriptOutputImpl scriptCode) {
		CompilationUnit compileUnit = new CompilationUnit();
	    compileUnit.addSource(scriptCode.getFullClassName(), scriptCode.getScriptSourceCode());
	    compileUnit.compile(Phases.CLASS_GENERATION);
	    compileUnit.setClassLoader(groovyCl);
	    
	    for (Object compileClass : compileUnit.getClasses()) {
	        GroovyClass groovyClass = (GroovyClass) compileClass;
	        callbacks.compiledGroovyClass(groovyCl, groovyClass);
	    }
	}

}
