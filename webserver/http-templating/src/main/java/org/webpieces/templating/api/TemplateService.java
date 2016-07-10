package org.webpieces.templating.api;

import org.webpieces.templating.impl.ProdTemplateService;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdTemplateService.class)
public interface TemplateService {

	Template loadTemplate(String packageStr, String templateClassName, String extension);

	
}
