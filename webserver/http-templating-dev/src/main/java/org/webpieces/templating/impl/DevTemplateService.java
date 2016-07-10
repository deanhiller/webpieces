package org.webpieces.templating.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.ScriptCode;

public class DevTemplateService implements TemplateService {

	private GroovyScriptGenerator scriptGen;
	private GroovyCompile groovyCompile;

	@Inject
	public DevTemplateService(GroovyScriptGenerator scriptGen, GroovyCompile groovyCompile) {
		this.scriptGen = scriptGen; 
		this.groovyCompile = groovyCompile;
	}

	@Override
	public Template loadTemplate(String packageStr, String templateClassName, String extension) {
		try {
			return loadTemplateImpl(packageStr, templateClassName, extension);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Template loadTemplateImpl(String packageStr, String templateClassName, String extension) throws IOException {
		String directory = "";
		if(!"".equals(packageStr))
			directory = "/"+packageStr.replace(".", "/");
		String fileName = templateClassName+"."+extension;
		String fullPath = directory+"/"+fileName;

		InputStream resource = DevTemplateService.class.getResourceAsStream(fullPath);
		if(resource == null)
			throw new FileNotFoundException("resource="+directory+"/"+fileName+" was not found in classpath");

		String viewSource = IOUtils.toString(resource, Charset.defaultCharset().name());

		String fullClassName = templateClassName+"_"+extension;
		if(!"".equals(packageStr))
			fullClassName = packageStr+"."+fullClassName;

		return createTemplate(fullClassName, viewSource);
	}

	private Template createTemplate(String fullClassName, String source) {
		ScriptCode scriptCode = scriptGen.generate(source, fullClassName);
		Class<?> compiledTemplate = groovyCompile.compile(scriptCode);
		return new TemplateImpl(compiledTemplate);
	}
}
