package org.webpieces.templating.impl;

import java.util.Map;

import org.webpieces.templating.api.TemplateResult;
import org.webpieces.templating.api.TemplateUtil;

public class TemplateResultImpl implements TemplateResult {

	private GroovyTemplateSuperclass t;

	public TemplateResultImpl(GroovyTemplateSuperclass t) {
		this.t = t;
	}

	@Override
	public String getSuperTemplateClassName() {
		String superTemplatePath = t.getSuperTemplateFilePath();
		return TemplateUtil.translateToProperFilePath(t, superTemplatePath);
	}
	
	@Override
	public String getTemplateClassName() {
		return t.getClass().getName();
	}
	
	@Override
	public Map<Object, Object> getTemplateProperties() {
		return t.getTemplateProperties();
	}

}
