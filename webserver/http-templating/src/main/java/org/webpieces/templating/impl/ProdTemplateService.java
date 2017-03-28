package org.webpieces.templating.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateResult;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;

public class ProdTemplateService implements TemplateService {

	private HtmlTagLookup lookup;
	private boolean isInitialized = false;
	protected RouterLookup urlLookup;

	@Inject
	public ProdTemplateService(RouterLookup urlLookup, HtmlTagLookup lookup) {
		this.urlLookup = urlLookup;
		this.lookup = lookup;
	}
	
	@Override
	public void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs) {
		Template template = loadTemplate(templatePath);
		runTemplate(template, out, pageArgs);
	}
	
	@Override
	public String loadAndRunTemplate(String templatePath, Map<String, Object> pageArgs,
			Map<Object, Object> setTagProps) {
		Template template = loadTemplate(templatePath);		
		return runTemplate(template, pageArgs, setTagProps);
	}
	
	protected final Template loadTemplate(String templatePath) {
		if(!templatePath.startsWith("/"))
			throw new IllegalArgumentException("templatePath must start with / and be absolute reference from base of classpath");
		else if(templatePath.contains("_"))
			throw new IllegalArgumentException("template names cannot contain _ in them(This is reserved for _extension in the classname).  name="+templatePath);
		
		if(!isInitialized) {
			lookup.initialize(this);
			isInitialized = true;
		}
		
		String fullClassName = TemplateUtil.convertTemplatePathToClass(templatePath);
		return loadTemplate(templatePath, fullClassName);
	}
	
	protected Template loadTemplate(String fullTemplatePath, String fullClassName) {
		ClassLoader cl = getClass().getClassLoader();
		try {
			Class<?> compiledTemplate = cl.loadClass(fullClassName);
			return new TemplateImpl(urlLookup, lookup, compiledTemplate);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	protected final void runTemplate(Template template, StringWriter out, Map<String, Object> pageArgs) {
		String result = runTemplate(template, pageArgs, new HashMap<>());
		out.write(result);
	}
	
    public String runTemplate(Template template, Map<String, Object> pageArgs, Map<Object, Object> setTagProps) {
		
		Map<String, Object> copy = new HashMap<>(pageArgs);
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		copy.put(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, writer);
		TemplateResult info = template.run(copy, setTagProps);

		//cache results of writer into templateProps for body so that template can use #{get 'body'}#
		Map<Object, Object> setTagProperties = info.getSetTagProperties();
		setTagProperties.put("body", out.toString());
		
		String className = info.getTemplateClassName();
		String templatePath = TemplateUtil.convertTemplateClassToPath(className);
		String superTemplateFilePath = info.getSuperTemplateClassName();
		try {
			if(superTemplateFilePath != null) {
				Template superTemplate = loadTemplate(superTemplateFilePath);
				return runTemplate(superTemplate, pageArgs, setTagProperties);
			} else
				return out.toString();
		} catch(Exception e) {
			throw new RuntimeException("template failed="+superTemplateFilePath+" called from template="
					+templatePath+" See below exception messages for more information", e);
		}
	}

}
