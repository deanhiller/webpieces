package org.webpieces.templating.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.source.ScriptOutputImpl;

import groovy.lang.GroovyClassLoader;

public class DevTemplateService extends ProdTemplateService implements TemplateService {

	private HtmlToJavaClassCompiler compiler;
	private TemplateCompileConfig config;
	private HtmlTagLookup htmlTagLookup;

	@Inject
	public DevTemplateService(HtmlTagLookup htmlTagLookup, HtmlToJavaClassCompiler compiler, TemplateCompileConfig config) {
		super(htmlTagLookup);
		this.htmlTagLookup = htmlTagLookup;
		this.compiler = compiler;
		this.config = config;
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
		String fullPath = templateClassName+"."+extension;
		if(!"".equals(packageStr)) {
			String directory = packageStr.replace(".", "/");
			fullPath = directory + "/" + fullPath;
		}
		fullPath = "/" + fullPath;
		
		InputStream resource = DevTemplateService.class.getResourceAsStream(fullPath);
		if(resource == null)
			throw new FileNotFoundException("resource="+fullPath+" was not found in classpath");
		else if(templateClassName.contains("_"))
			throw new IllegalArgumentException("template names cannot contain _ in them.  name="+templateClassName);
		
		String viewSource = IOUtils.toString(resource, config.getFileEncoding().name());

		String fullClassName = templateClassName+"_"+extension;
		if(!"".equals(packageStr))
			fullClassName = packageStr+"."+fullClassName;

		Class<?> compiledTemplate = createTemplate(fullClassName, viewSource);
		
		return new TemplateImpl(htmlTagLookup, compiledTemplate);
	}

	private Class<?> createTemplate(String fullClassName, String source) throws ClassNotFoundException {
		GroovyClassLoader cl = new GroovyClassLoader();
		
		ScriptOutputImpl scriptCode = compiler.compile(fullClassName, source, groovy -> defineClass(cl, groovy));
		
		return cl.loadClass(scriptCode.getFullClassName());
	}

	private Void defineClass(GroovyClassLoader cl, GroovyClass groovyClass) {
        cl.defineClass(groovyClass.getName(), groovyClass.getBytes());
		return null;
	}

}
