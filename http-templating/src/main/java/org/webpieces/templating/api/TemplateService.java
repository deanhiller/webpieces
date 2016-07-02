package org.webpieces.templating.api;

import org.webpieces.templating.impl.TemplateServiceImpl;

import com.google.inject.ImplementedBy;

@ImplementedBy(TemplateServiceImpl.class)
public interface TemplateService {

	Template loadTemplate(String packageStr, String templateClassName, String extension);

	
}
