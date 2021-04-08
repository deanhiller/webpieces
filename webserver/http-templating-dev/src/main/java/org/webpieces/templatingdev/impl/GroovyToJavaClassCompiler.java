package org.webpieces.templatingdev.impl;

import javax.inject.Inject;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templatingdev.api.CompileCallback;
import org.webpieces.templatingdev.impl.source.ScriptOutputImpl;
import org.webpieces.util.compiling.GroovyCompiling;
import org.webpieces.util.exceptions.SneakyThrow;

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
			throw SneakyThrow.sneak(e);
		}
	}

	private void compileImpl(GroovyClassLoader groovyCl, ScriptOutputImpl scriptCode) {
		
		try {
			GroovyCompiling.setCompilingGroovy(true);
			
			CompilationUnit compileUnit = new CompilationUnit();
		    compileUnit.addSource(scriptCode.getFullClassName(), scriptCode.getScriptSourceCode());
		    compileUnit.compile(Phases.CLASS_GENERATION);
		    compileUnit.setClassLoader(groovyCl);
		    
		    for (Object compileClass : compileUnit.getClasses()) {
		        GroovyClass groovyClass = (GroovyClass) compileClass;
		        callbacks.compiledGroovyClass(groovyCl, groovyClass);
		    }
		} finally {
			GroovyCompiling.setCompilingGroovy(false);			
		}
	}

}
