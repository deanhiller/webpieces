package org.webpieces.templating.impl;

import java.util.Map;

import org.webpieces.templating.api.TemplateResult;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.util.file.ClassUtil;

public class TemplateResultImpl implements TemplateResult {

	private GroovyTemplateSuperclass t;

	public TemplateResultImpl(GroovyTemplateSuperclass t) {
		this.t = t;
	}

	@Override
	public String getSuperTemplateClassName() {
		String superTemplatePath = t.getSuperTemplateFilePath();
		if(superTemplatePath == null)
			return null;
		else if(superTemplatePath.startsWith("/"))
			return TemplateUtil.convertTemplatePathToClass(superTemplatePath);
		
		//get the package this template was in...
		String name = t.getClass().getName();
		int lastIndexOf = name.lastIndexOf(".");
		String packageCtx = "";
		if(lastIndexOf > 0) {
			packageCtx = name.substring(0, lastIndexOf);
		}
		
		//had to do this since ClassUtil.translate deals in . and returns a classname so this makes it compatible..
		String superTemplatePathWithClassName = removeUnderDotFromFileName(superTemplatePath);
		String fullTempateClassName = ClassUtil.translate(packageCtx, superTemplatePathWithClassName);
		return fullTempateClassName;
	}

	private String removeUnderDotFromFileName(String superTemplatePath) {
		int lastIndexOfSlash = superTemplatePath.lastIndexOf("/");
		String pathWithNoFile = "";
		String fileName = superTemplatePath;
		if(lastIndexOfSlash > 0) {
			pathWithNoFile = superTemplatePath.substring(0, lastIndexOfSlash);
			fileName = superTemplatePath.substring(lastIndexOfSlash);
		}
		fileName = fileName.replace(".", "_");
		
		String superTemplatePathWithClassName = pathWithNoFile+fileName;
		return superTemplatePathWithClassName;
	}
	
	@Override
	public String getTemplateClassName() {
		return t.getClass().getName();
	}
	
	@Override
	public Map<?,?> getTemplateProperties() {
		return t.getTemplateProperties();
	}

}
