package org.webpieces.templating.api;

import org.webpieces.templating.impl.TemplateEngineImpl;

import com.google.inject.ImplementedBy;

@ImplementedBy(TemplateEngineImpl.class)
public interface TemplateEngine {

	/**
	 * 
	 * @param className
	 * @param textFileSource The source of the html file or json file that is the template to be converted to script source and
	 * then to a runnable template
	 * 
	 * @return
	 */
	public Template createTemplate(String className, String textFileSource);
	
}
