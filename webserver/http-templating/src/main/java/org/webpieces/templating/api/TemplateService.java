package org.webpieces.templating.api;

import java.io.StringWriter;
import java.util.Map;

import org.webpieces.templating.impl.ProdTemplateService;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdTemplateService.class)
public interface TemplateService {

	Template loadTemplate(String templatePath);

	void runTemplate(Template template, StringWriter out, Map<String, Object> pageArgs, ReverseUrlLookup lookup);
	
}
