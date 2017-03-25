package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Binding;
import groovy.lang.Closure;

public abstract class TemplateLoaderTag implements HtmlTag {

	protected TemplateService svc;
	
	@Override
	public void runTag(Map<Object, Object> tagArgs, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass parentTemplate,
			String srcLocation) {
		Binding binding = parentTemplate.getBinding();
		@SuppressWarnings("unchecked")
		Map<String, Object> pageArgs = binding.getVariables();

		Map<String, Object> customTagArgs = convertTagArgs(tagArgs, pageArgs, body, srcLocation);
		
		String filePath = getFilePath(parentTemplate, tagArgs, srcLocation);
		Template template = svc.loadTemplate(filePath);

		Map<Object, Object> setTagProps = parentTemplate.getSetTagProperties();
		String s = svc.runTemplate(template, customTagArgs, setTagProps);
		out.print(s);
	}
	
	protected abstract Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs, Closure<?> body, String srcLocation);

	protected String getFilePath(GroovyTemplateSuperclass callingTemplate, Map<Object, Object> args, String srcLocation) {
        Object name = args.get("defaultArgument");
        if(name == null)
        	throw new IllegalArgumentException("#{"+getName()+"/}# tag must contain a template name like #{"+getName()+" '../template.html'/}#. "+srcLocation);

        String path = TemplateUtil.translateToProperFilePath(callingTemplate, name.toString());
		return path;
	}
	
	/**
	 * This is a bit nasty circular dependency but this tag is special and whether in dev or prod mode needs
	 * to re-use all the loadTemplate/runTemplate logic
	 * 
	 * TEmplateService -> HtmlTagLookup -> HtmlFileTag -> TemplateService
	 * 
	 * @param svc
	 */
	public void initialize(TemplateService svc) {
		this.svc = svc;
	}
}
