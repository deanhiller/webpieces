package org.webpieces.templating.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.templating.impl.source.ScriptOutputImpl;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;

import groovy.lang.GroovyClassLoader;

public class DevTemplateService extends ProdTemplateService {

	private HtmlToJavaClassCompiler compiler;
	private TemplateCompileConfig config;
	private HtmlTagLookup htmlTagLookup;

	private ThreadLocal<OurGroovyClassLoader> currentCl = new ThreadLocal<>();
	
	@Inject
	public DevTemplateService(
			RouterLookup urlLookup, 
			HtmlTagLookup htmlTagLookup, 
			HtmlToJavaClassCompiler compiler, 
			TemplateCompileConfig config) {
		super(urlLookup, htmlTagLookup);
		this.htmlTagLookup = htmlTagLookup;
		this.compiler = compiler;
		this.config = config;
	}

	@Override
	public void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs) {
		//
		GroovyClassLoader startingCl = currentCl.get();
		if(startingCl == null)
			currentCl.set(new OurGroovyClassLoader());
		
		try {
			Template template = loadTemplate(templatePath);
			runTemplate(template, out, pageArgs);
		} finally {
			if(startingCl == null) {
				//startingCl is the flag for when we created the original Cl
				currentCl.set(null);
			}
		}
	}
	
	protected Template loadTemplate(String fullTemplatePath, String fullClassName) {
		//this is a recursive function.  Run TestFieldTag.java to see
		try {
			return loadTemplateImpl(currentCl.get(), fullTemplatePath, fullClassName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Template loadTemplateImpl(OurGroovyClassLoader cl, String fullTemplatePath, String templateFullClassName) throws IOException, ClassNotFoundException {
		if(config.isMustReadClassFromFileSystem()) {
			//shortcut for tests that use PlatformOverridesForTest to ensure we test the production groovy class files AND
			//it ensures we give code coverage numbers on those class files as well.
			Class<?> compiledTemplate = DevTemplateService.class.getClassLoader().loadClass(templateFullClassName);
			return new TemplateImpl(urlLookup, htmlTagLookup, compiledTemplate);
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
	
			Class<?> compiledTemplate = createTemplate(cl, templateFullClassName, viewSource);
			
			return new TemplateImpl(urlLookup, htmlTagLookup, compiledTemplate);
		}
	}

	private Class<?> createTemplate(OurGroovyClassLoader cl, String fullClassName, String source) throws ClassNotFoundException {
		if(!cl.isClassDefined(fullClassName))
			compiler.compile(cl, fullClassName, source);
		return cl.loadClass(fullClassName);
	}
}
