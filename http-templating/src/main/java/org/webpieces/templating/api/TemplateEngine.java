package org.webpieces.templating.api;

import java.io.Reader;
import java.util.Map;

public interface TemplateEngine {

	public String createPage(Reader reader, Map<String, Object> arguments);

	public void createGroovySource(String resourcePath);
	
}
