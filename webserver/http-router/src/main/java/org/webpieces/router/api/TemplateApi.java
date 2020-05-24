package org.webpieces.router.api;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.extension.HtmlTagCreator;

public interface TemplateApi {

	void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs);

	//TemplateUtil.convertTemplateClassToPath(className);
	String convertTemplateClassToPath(String fullClass);

	void installCustomTags(Set<HtmlTagCreator> tagCreators);
}
