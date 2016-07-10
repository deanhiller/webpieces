package org.webpieces.templating.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.ScriptCode;

import groovy.lang.GroovyClassLoader;

public class DevTemplateService implements TemplateService {

	private HtmlToJavaClassCompiler compiler;

	@Inject
	public DevTemplateService(HtmlToJavaClassCompiler compiler) {
		this.compiler = compiler;
	}

	@Override
	public Template loadTemplate(String packageStr, String templateClassName, String extension) {
		try {
			return loadTemplateImpl(packageStr, templateClassName, extension);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Template loadTemplateImpl(String packageStr, String templateClassName, String extension) throws IOException, ClassNotFoundException {
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

		Class<?> compiledTemplate = createTemplate(fullClassName, viewSource);
		
		return new TemplateImpl(compiledTemplate);
	}

	private Class<?> createTemplate(String fullClassName, String source) throws ClassNotFoundException {
		GroovyClassLoader cl = new GroovyClassLoader();
		
		ScriptCode scriptCode = compiler.compile(fullClassName, source, groovy -> defineClass(cl, groovy));
		
		return cl.loadClass(scriptCode.getFullClassName());
	}

	private Object defineClass(GroovyClassLoader cl, GroovyClass groovyClass) {
        cl.defineClass(groovyClass.getName(), groovyClass.getBytes());

		return null;
	}

}
