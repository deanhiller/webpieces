package org.webpieces.templating.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateInfo;
import org.webpieces.templating.api.TemplateService;

public class ProdTemplateService implements TemplateService {

	private HtmlTagLookup lookup;

	@Inject
	public ProdTemplateService(HtmlTagLookup lookup) {
		this.lookup = lookup;
	}
	
	@Override
	public Template loadTemplate(String packageStr, String templateClassName, String extension) {
		String fullTemplateName = templateClassName;
		if(!"".equals(packageStr))
			fullTemplateName = packageStr + "." + templateClassName;

		if(!"".equals(extension))
			fullTemplateName = fullTemplateName+"_"+extension;
		
		return loadTemplate(fullTemplateName);
	}

	private Template loadTemplate(String fullTemplateName) {
		ClassLoader cl = getClass().getClassLoader();
		try {
			Class<?> compiledTemplate = cl.loadClass(fullTemplateName);
			return new TemplateImpl(lookup, compiledTemplate);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runTemplate(Template template, StringWriter out, Map<String, Object> pageArgs) {
		Map<String, Object> copy = new HashMap<>(pageArgs);
		TemplateInfo info = template.run(copy, out);

		String className = info.getTemplateClass();
		String superTemplateFilePath = info.getSuperTemplateFilePath();
		try {
			if(superTemplateFilePath != null) {
				Template superTemplate = loadTemplate(superTemplateFilePath);
				runTemplate(superTemplate, out, pageArgs);
			}
		} catch(Exception e) {
			throw new RuntimeException("template failed="+superTemplateFilePath+" called from template="
					+className+" See below exception messages for more information", e);
		}
	}

}
