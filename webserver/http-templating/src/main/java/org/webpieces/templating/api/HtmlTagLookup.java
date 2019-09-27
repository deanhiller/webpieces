package org.webpieces.templating.api;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.templating.impl.tags.BootstrapModalTag;
import org.webpieces.templating.impl.tags.ExtendsTag;
import org.webpieces.templating.impl.tags.FieldTag;
import org.webpieces.templating.impl.tags.FormTag;
import org.webpieces.templating.impl.tags.HtmlGetTag;
import org.webpieces.templating.impl.tags.HtmlSetTag;
import org.webpieces.templating.impl.tags.JsActionTag;
import org.webpieces.templating.impl.tags.OptionTag;
import org.webpieces.templating.impl.tags.RenderPageArgsTag;
import org.webpieces.templating.impl.tags.RenderTagArgsTag;
import org.webpieces.templating.impl.tags.ScriptTag;
import org.webpieces.templating.impl.tags.StyleSheetTag;
import org.webpieces.templating.impl.tags.TemplateLoaderTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlTagLookup {
	private static final Logger log = LoggerFactory.getLogger(HtmlTagLookup.class);
	
	private Map<String, HtmlTag> tags = new HashMap<>();
	protected ConverterLookup converter;
	
	@Inject
	public HtmlTagLookup(
		TemplateConfig config, 
		RouterLookup lookup, 
		ConverterLookup converter
	) {
		this.converter = converter;
		put(new OptionTag(converter));
		put(new HtmlSetTag());
		put(new HtmlGetTag());
		put(new ExtendsTag());
		put(new FormTag(config.getDefaultFormAcceptEncoding()));
		put(new RenderTagArgsTag());
		put(new RenderPageArgsTag());
		put(new JsActionTag());
		put(new StyleSheetTag(lookup));
		put(new ScriptTag(lookup));
		put(new BootstrapModalTag());
		put(new FieldTag(converter, "/org/webpieces/templating/impl/field.tag"));		
	}
	
	protected void put(HtmlTag tag) {
		HtmlTag htmlTag = tags.get(tag.getName());
		if(htmlTag != null)
			log.warn("You are overriding Tag="+tag.getName()+" from class="+htmlTag.getClass()+" to your class="+tag.getClass());
		else
			log.info("adding tag="+tag.getName());
		tags.put(tag.getName(), tag);
	}

	public HtmlTag lookup(String tagName) {
		HtmlTag tag = tags.get(tagName);
		return tag;
	}

	/**
	 * A nasty circular dependency so we initialize it on the firstTemplate loaded in the application
	 * 
	 * @param templateService
	 */
	public void initialize(TemplateService templateService) {
		for(HtmlTag tag : tags.values()) {
			if(tag instanceof TemplateLoaderTag) {
				TemplateLoaderTag fileTag = (TemplateLoaderTag) tag;
				fileTag.initialize(templateService);
			}
		}
	}
}
