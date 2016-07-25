package org.webpieces.templating.impl;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.ScriptOutputImpl;

public class HtmlToJavaClassCompiler {
	private GroovyScriptGenerator scriptGen;
	private GroovyToJavaClassCompiler groovyCompile;

	@Inject
	public HtmlToJavaClassCompiler(GroovyScriptGenerator scriptGen, GroovyToJavaClassCompiler groovyCompile) {
		this.scriptGen = scriptGen; 
		this.groovyCompile = groovyCompile;
	}
	
	public ScriptOutputImpl compile(String fullClassName, String source, Consumer<GroovyClass> compiledCallback) {
		String filePath = fullClassName.replace(".", "/").replace("_", ".");
		
		ScriptOutputImpl scriptCode = scriptGen.generate(filePath, source, fullClassName);
		groovyCompile.compile(scriptCode, compiledCallback);
		return scriptCode;
	}
}
