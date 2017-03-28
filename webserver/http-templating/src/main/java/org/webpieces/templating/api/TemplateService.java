package org.webpieces.templating.api;

import java.io.StringWriter;
import java.util.Map;

import org.webpieces.templating.impl.ProdTemplateService;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdTemplateService.class)
public interface TemplateService {

	void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs);
	
	//Template loadTemplate(String templatePath);

	//void runTemplate(Template template, StringWriter out, Map<String, Object> pageArgs);
	
	String loadAndRunTemplate(String templatePath, Map<String, Object> pageArgs, Map<Object, Object> setTagProps);
	//String runTemplate(Template template, Map<String, Object> pageArgs, Map<Object, Object> setTagProps);
}
