package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class CustomTag implements HtmlTag {

	private String file;
	private TemplateService svc;
	private String name;

	public CustomTag(String file) {
		if(!file.endsWith(".tag"))
			throw new IllegalArgumentException("tag file must end in .tag="+file);
		else if(!file.startsWith("/"))
			throw new IllegalArgumentException("tag file path must begin with / which is the root of the classpath");
		this.file = file;
		
		int extensionIndex = file.lastIndexOf(".");
		this.name = file.substring(0, extensionIndex);
		int lastSlashIndex = this.name.lastIndexOf("/");
		if(lastSlashIndex > 0) {
			this.name = this.name.substring(lastSlashIndex+1);
		}
	}

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass parentTemplate,
			String srcLocation) {
		Map<Object, Object> templateProps = parentTemplate.getTemplateProperties();
		
		//Map<String, Object> customTagArgs = overridePageArgsWithTagArgs(args, parentTemplate);
		Map<String, Object> customTagArgs = convertTagArgs(args);
		
		ReverseUrlLookup lookup = parentTemplate.getUrlLookup();
		
		Template template = svc.loadTemplate(file);

		String s = svc.runTemplate(template, customTagArgs, templateProps, lookup);
		out.print(s);
	}

	private Map<String, Object> convertTagArgs(Map<Object, Object> args) {
		Map<String, Object> tagArgs = new HashMap<>();
		for(Map.Entry<Object, Object> entry : args.entrySet()) {
			String key = entry.getKey().toString();
			tagArgs.put(key, entry.getValue());
		}
		return tagArgs;
	}

	//NOT sure if this is desired or not.  it's simpler to keep the scopes separate so there are not conflicts in vars which would be very
	//confusing when someone as a tag argument name the same as a page argument name and gets confused.  leaving it here for easy revival though.
//	private Map<String, Object> overridePageArgsWithTagArgs(Map<Object, Object> tagArgs, GroovyTemplateSuperclass parentTemplate) {
//		Binding binding = parentTemplate.getBinding();
//		@SuppressWarnings("unchecked")
//		Map<String, Object> pageArgs = binding.getVariables();
//		Map<String, Object> copy = new HashMap<>(pageArgs);
//		
//		boolean isFailFast = true;
//		Object failFast = tagArgs.get("_failfast");
//		if(failFast != null && failFast instanceof Boolean && !((Boolean)failFast))
//			failFast = false;
//		
//		for(Map.Entry<Object, Object> entry : tagArgs.entrySet()) {
//			String key = entry.getKey().toString();
//			
//			if(isFailFast) {
//				Object value = copy.get(key);
//				if(value != null)
//					throw new IllegalStateException("page argument="+key+" from the controller conflict with tag argument of the "
//							+ "same name.  Either add _failfast:false to the tag argument list or rename the controller's page "
//							+ "argument name or the custom tags argument name.  custom tag="+name);
//			}
//			
//			copy.put(key, entry.getValue());
//		}
//		return copy;
//	}

	@Override
	public String getName() {
		return name;
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
