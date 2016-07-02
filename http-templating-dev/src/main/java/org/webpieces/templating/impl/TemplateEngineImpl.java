package org.webpieces.templating.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateEngine;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.ScriptCode;

@Singleton
public class TemplateEngineImpl implements TemplateEngine {

	private GroovyScriptGenerator scriptGen;
	private GroovyCompile groovyCompile;

	@Inject
	public TemplateEngineImpl(GroovyScriptGenerator scriptGen, GroovyCompile groovyCompile) {
		this.scriptGen = scriptGen; 
		this.groovyCompile = groovyCompile;
	}
	
	@Override
	public Template createTemplate(String fullClassName, String source) {
		ScriptCode scriptCode = scriptGen.generate(source, fullClassName);
		Class<?> compiledTemplate = groovyCompile.compile(scriptCode);
		return new TemplateImpl(compiledTemplate);
	}

//	private StreamingTemplateEngine engine = new groovy.text.StreamingTemplateEngine();
//	
//	@Override
//	public String createPage(Reader reader, Map<String, Object> arguments) {
//		try {
//			return createPageImpl(reader, arguments);
//		} catch (CompilationFailedException e) {
//			throw new RuntimeException(e);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private String createPageImpl(Reader reader, Map<String, Object> arguments) 
//			throws CompilationFailedException, ClassNotFoundException, IOException {
//		
//		Template template = engine.createTemplate(reader);
////		def binding = [
////	    firstname : "Grace",
////	    lastname  : "Hopper",
////	    accepted  : true,
////	    title     : 'Groovy for COBOL programmers'
////	]
////
//		Writable writer = template.make(arguments);
//		
//		StringWriter strWriter = new StringWriter();
//		writer.writeTo(strWriter);
//		return strWriter.toString();
//	}
//
//	@Override
//	public void createGroovySource(String resourcePath) {
//	}
}
