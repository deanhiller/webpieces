package org.webpieces.templating.impl;

import java.util.function.Consumer;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.impl.source.ScriptOutputImpl;

import groovy.lang.GroovyClassLoader;

public class GroovyToJavaClassCompiler {

	public void compile(ScriptOutputImpl scriptCode, Consumer<GroovyClass> compiledCallback) {
		try {
			compileImpl(scriptCode, compiledCallback);
			//F'ing checked exceptions should have been runtime so I don't have all this cruft in my app...
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	private void compileImpl(ScriptOutputImpl scriptCode, Consumer<GroovyClass> compiledCallback) {
		GroovyClassLoader cl = new GroovyClassLoader();
		CompilationUnit compileUnit = new CompilationUnit();
	    compileUnit.addSource(scriptCode.getFullClassName(), scriptCode.getScriptSourceCode());
	    compileUnit.compile(Phases.CLASS_GENERATION);
	    compileUnit.setClassLoader(cl);

	    for (Object compileClass : compileUnit.getClasses()) {
	        GroovyClass groovyClass = (GroovyClass) compileClass;
	        compiledCallback.accept(groovyClass);
	    }
	}

}
