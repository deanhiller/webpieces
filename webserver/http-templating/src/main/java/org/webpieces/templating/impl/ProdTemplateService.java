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
		
		String superclass = info.getSuperTemplate();
		if(superclass != null) {
			Template superTemplate = loadTemplate("", "", "");
			runTemplate(superTemplate, out, pageArgs);
			throw new UnsupportedOperationException("not done yet");
		}
	}

}
