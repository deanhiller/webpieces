package org.webpieces.router.api;

import java.io.StringWriter;
import java.util.Map;

public interface TemplateApi {

	void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs);

	//TemplateUtil.convertTemplateClassToPath(className);
	String convertTemplateClassToPath(String fullClass);

}
