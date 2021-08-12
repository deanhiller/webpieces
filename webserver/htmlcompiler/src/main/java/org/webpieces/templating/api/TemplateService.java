package org.webpieces.templating.api;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.templating.impl.ProdTemplateService;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdTemplateService.class)
public interface TemplateService {

	void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs);

	/**
	 * Purely for tag use only.
	 * 
	 * we could hide this as strictly speaking, this is not for any webserver to call but for tags to use
	 */
	String loadAndRunTemplate(String templatePath, Map<String, Object> pageArgs, Map<Object, Object> setTagProps);
	
	/**
	 * Special way to install more tags.  Development server calls this after recompiles to re-install tags
	 */
	void install(Set<HtmlTagCreator> htmlCreators);

}
