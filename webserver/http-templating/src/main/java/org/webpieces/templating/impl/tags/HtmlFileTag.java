package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Binding;
import groovy.lang.Closure;

public class HtmlFileTag implements HtmlTag {

	private String file;
	private TemplateService svc;
	private String name;

	public HtmlFileTag(String file) {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void runTag(Map<?, ?> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass parentTemplate,
			String srcLocation) {
		Map<?, ?> templateProps = parentTemplate.getTemplateProperties();
		
		Map theArgs = overridePageArgsWithTagArgs(args, parentTemplate);
		
		ReverseUrlLookup lookup = parentTemplate.getUrlLookup();
		
		Template template = svc.loadTemplate(file);
		
		StringWriter wr1 = new StringWriter();
		PrintWriter writer = new PrintWriter(wr1);
		theArgs.put(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, writer);
		template.run(theArgs, templateProps, lookup);
		out.print(wr1.toString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map overridePageArgsWithTagArgs(Map<?, ?> tagArgs, GroovyTemplateSuperclass parentTemplate) {
		Binding binding = parentTemplate.getBinding();
		Map pageArgs = binding.getVariables();
		Map copy = new HashMap<>(pageArgs);
		
		boolean isFailFast = true;
		Object failFast = tagArgs.get("_failfast");
		if(failFast != null && failFast instanceof Boolean && !((Boolean)failFast))
			failFast = false;
		
		for(Map.Entry<?, ?> entry : tagArgs.entrySet()) {
			String key = entry.getKey().toString();
			
			if(isFailFast) {
				Object value = copy.get(key);
				if(value != null)
					throw new IllegalStateException("page argument="+key+" from the controller conflict with tag argument of the "
							+ "same name.  Either add _failfast:false to the tag argument list or rename the controller's page "
							+ "argument name or the custom tags argument name.  custom tag="+name);
			}
			
			copy.put(key, entry.getValue());
		}
		return copy;
	}

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
