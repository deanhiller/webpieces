package org.webpieces.templating.impl;

import javax.inject.Inject;

import org.webpieces.templating.api.CompileCallback;
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
	
	public ScriptOutputImpl compile(String fullClassName, String source, CompileCallback callbacks) {
		String filePath = fullClassName.replace(".", "/").replace("_", ".");
		
		ScriptOutputImpl scriptCode = scriptGen.generate(filePath, source, fullClassName, callbacks);
		try {
			groovyCompile.compile(scriptCode, callbacks);
		} catch(Exception e) {
			throw new RuntimeException("Generated a groovy script file but compilation failed for file="
					+filePath+" Script code generated=\n\n"+scriptCode.getScriptSourceCode(), e);
		}
		return scriptCode;
	}
}
