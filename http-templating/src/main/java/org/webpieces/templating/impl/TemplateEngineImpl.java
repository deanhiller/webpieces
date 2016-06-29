package org.webpieces.templating.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.webpieces.templating.api.TemplateEngine;

import groovy.lang.Writable;
import groovy.text.StreamingTemplateEngine;
import groovy.text.Template;

public class TemplateEngineImpl implements TemplateEngine {

	private StreamingTemplateEngine engine = new groovy.text.StreamingTemplateEngine();
	
	@Override
	public String createPage(Reader reader, Map<String, Object> arguments) {
		try {
			return createPageImpl(reader, arguments);
		} catch (CompilationFailedException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String createPageImpl(Reader reader, Map<String, Object> arguments) 
			throws CompilationFailedException, ClassNotFoundException, IOException {
		
		Template template = engine.createTemplate(reader);
//		def binding = [
//	    firstname : "Grace",
//	    lastname  : "Hopper",
//	    accepted  : true,
//	    title     : 'Groovy for COBOL programmers'
//	]
//
		Writable writer = template.make(arguments);
		
		StringWriter strWriter = new StringWriter();
		writer.writeTo(strWriter);
		return strWriter.toString();
	}

	@Override
	public void createGroovySource(String resourcePath) {
	}
}
