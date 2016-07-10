package org.webpieces.templating.impl;

import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;

public class ProdTemplateService implements TemplateService {

	@Override
	public Template loadTemplate(String packageStr, String templateClassName, String extension) {
		throw new UnsupportedOperationException("not there yet, but would load p="+packageStr+" template="+templateClassName+" ext="+extension);
	}

}
