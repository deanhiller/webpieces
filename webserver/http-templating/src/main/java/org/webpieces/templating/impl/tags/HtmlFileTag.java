package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateResult;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

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

	@Override
	public void runTag(Map<?, ?> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass parentTemplate,
			String srcLocation) {
		Map<?, ?> templateProps = parentTemplate.getTemplateProperties();
		Map<String, Object> theArgs = new HashMap<>();
		//TODO: modify template.run signature so we don't need to do this at all...
		for(Map.Entry<?, ?> entry : args.entrySet()) {
			theArgs.put(entry.toString(), entry.getValue());
		}
		
		ReverseUrlLookup lookup = parentTemplate.getUrlLookup();
		
		Template template = svc.loadTemplate(file);
		TemplateResult result = template.run(theArgs, templateProps, lookup);
		out.print(result.getResult());
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
