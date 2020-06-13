package org.webpieces.templatingdev.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.impl.ProdTemplateService;
import org.webpieces.templating.impl.TemplateImpl;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;

@Singleton
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
	public void initialize() {
		lookup.initialize(this);
	}
	
	@Override
	public void loadAndRunTemplateImpl(String templatePath, StringWriter out, Map<String, Object> pageArgs) {
		//TODO: big nit and for fun, we should look into recreating OurGroovyClassLoader ONLY when
		//the html files have changed.  to do this, I think we would have to save a Holder object with the first 
		//classloader and only swap him on changes to the html files instead of on every request.  I think if we
		//do that, the ThreadLocal may go away as well.  This may need some manual testing and we may have to add
		// more change files during test like we did for the controller testing in TestDevRefreshPageWithNoRestarting.java
		
		//prod knows nothing about groovy or the Groovy classloader so we keep it that way 
		//by setting the classloader used on the thread.  This is NOT A recursive function like the below.
		currentCl.set(new OurGroovyClassLoader());
		
		try {
			super.loadAndRunTemplateImpl(templatePath, out, pageArgs);
		} finally {
			currentCl.set(null);
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
			theResource = new VirtualFileClasspath(fullTemplatePath, DevTemplateService.class, false);
		
		if(!theResource.exists()) {
			try {
				//This is for plugins in the jar file that someone's webapp is using so there is no *.html files in that jar
				//and it only has xxxxx_html.class files(precompiled templates)
				return super.loadTemplate(fullTemplatePath, templateFullClassName);
			} catch(ClassNotFoundException e) {
				//ok, class is not found, throw original, file not found exception
				FileNotFoundException exc = new FileNotFoundException("resource="+fullTemplatePath+" was not found in classpath AND corresponding *.class file not found too");
				exc.initCause(e);
				throw exc;
			}
		}
		
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
