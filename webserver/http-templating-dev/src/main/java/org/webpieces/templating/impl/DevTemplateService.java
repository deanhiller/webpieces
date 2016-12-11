package org.webpieces.templating.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.source.ScriptOutputImpl;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;

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
	protected Template loadTemplate(String fullTemplatePath, String fullClassName) {
		try {
			return loadTemplateImpl(fullTemplatePath, fullClassName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Template loadTemplateImpl(String fullTemplatePath, String templateFullClassName) throws IOException, ClassNotFoundException {
		if(config.isMustReadClassFromFileSystem()) {
			//shortcut for tests that use PlatformOverridesForTest to ensure we test the production groovy class files AND
			//it ensures we give code coverage numbers on those class files as well.
			Class<?> compiledTemplate = DevTemplateService.class.getClassLoader().loadClass(templateFullClassName);
			return new TemplateImpl(htmlTagLookup, compiledTemplate);
		}
		
		VirtualFile theResource = null;
		List<VirtualFile> srcPaths = config.getSrcPaths();
		for(VirtualFile f : srcPaths) {
			VirtualFile child = f.child(fullTemplatePath.substring(1));//take off the leading '/' so it is a relative path
			if(child.exists()) {
				theResource = child;
				break;
			}
		}

		if(theResource == null) 
			theResource = new VirtualFileClasspath(fullTemplatePath, DevTemplateService.class);
		
		if(!theResource.exists())
			throw new FileNotFoundException("resource="+fullTemplatePath+" was not found in classpath");
		
		try(InputStream resource = theResource.openInputStream()) {
			String viewSource = IOUtils.toString(resource, config.getFileEncoding().name());
	
			Class<?> compiledTemplate = createTemplate(templateFullClassName, viewSource);
			
			return new TemplateImpl(htmlTagLookup, compiledTemplate);
		}
	}

	private Class<?> createTemplate(String fullClassName, String source) throws ClassNotFoundException {
		GroovyClassLoader cl = new GroovyClassLoader();
		
		ScriptOutputImpl scriptCode = compiler.compile(fullClassName, source, new DevTemplateCompileCallback(cl));
		
		return cl.loadClass(scriptCode.getFullClassName());
	}

	private static class DevTemplateCompileCallback implements CompileCallback {

		private GroovyClassLoader cl;

		public DevTemplateCompileCallback(GroovyClassLoader cl) {
			this.cl = cl;
		}

		@Override
		public void compiledGroovyClass(GroovyClass groovyClass) {
			cl.defineClass(groovyClass.getName(), groovyClass.getBytes());	
		}

		@Override
		public void routeIdFound(String routeId, List<String> argNames, String sourceLocation) {
			//validate??
		}
		
	}
}
