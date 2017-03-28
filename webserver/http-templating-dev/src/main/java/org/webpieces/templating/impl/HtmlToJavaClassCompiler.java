package org.webpieces.templating.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.inject.Inject;

import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.ScriptOutputImpl;

import groovy.lang.GroovyClassLoader;

public class HtmlToJavaClassCompiler {
	private GroovyScriptGenerator scriptGen;
	private GroovyToJavaClassCompiler groovyCompile;
	private TemplateCompileConfig config;

	@Inject
	public HtmlToJavaClassCompiler(
			GroovyScriptGenerator scriptGen, 
			GroovyToJavaClassCompiler groovyCompile,
			TemplateCompileConfig config) {
		this.scriptGen = scriptGen; 
		this.groovyCompile = groovyCompile;
		this.config = config;
	}
	
	public ScriptOutputImpl compile(GroovyClassLoader cl, String fullClassName, String source) {
		String filePath = fullClassName.replace(".", "/").replace("_", ".");
		
		ScriptOutputImpl scriptCode = scriptGen.generate(filePath, source, fullClassName);
		
		if(config.getGroovySrcWriteDirectory() != null)
			writeSourceFile(scriptCode, fullClassName);
		
		try {
			groovyCompile.compile(cl, scriptCode);
		} catch(Exception e) {
			throw new RuntimeException("Generated a groovy script file but compilation failed for file="
					+filePath+" Script code generated=\n\n"+scriptCode.getScriptSourceCode(), e);
		}
		return scriptCode;
	}

	private void writeSourceFile(ScriptOutputImpl scriptCode, String fullClassName) {
		try {
			writeSourceFile2(scriptCode, fullClassName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeSourceFile2(ScriptOutputImpl scriptCode, String fullClassName) throws FileNotFoundException, IOException {
		String className = fullClassName;
		int index;
		File currentDir = config.getGroovySrcWriteDirectory();
		while((index = className.indexOf(".")) >= 0) {
			String dir = className.substring(0, index);
			className = className.substring(index+1);
			currentDir = new File(currentDir, dir);
		}
		
		if(!currentDir.exists())
			currentDir.mkdirs();
		
		File groovySrcFile = new File(currentDir, className+".groovy");
		Charset fileEncoding = config.getFileEncoding();

		try (FileOutputStream str = new FileOutputStream(groovySrcFile);
			OutputStreamWriter writer = new OutputStreamWriter(str, fileEncoding.name())) {
			
			writer.write(scriptCode.getScriptSourceCode());
		}
	}
}
