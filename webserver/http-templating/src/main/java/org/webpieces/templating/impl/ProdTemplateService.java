package org.webpieces.templating.impl;

import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;

public class ProdTemplateService implements TemplateService {

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
			return new TemplateImpl(compiledTemplate);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
