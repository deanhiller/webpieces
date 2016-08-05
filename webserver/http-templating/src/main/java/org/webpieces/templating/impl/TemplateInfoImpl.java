package org.webpieces.templating.impl;

import org.webpieces.templating.api.TemplateInfo;

public class TemplateInfoImpl implements TemplateInfo {

	private GroovyTemplateSuperclass t;

	public TemplateInfoImpl(GroovyTemplateSuperclass t) {
		this.t = t;
	}

	@Override
	public String getSuperTemplate() {
		return null;
	}
	
	

}
