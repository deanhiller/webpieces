package org.webpieces.templating.api;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.templating.impl.tags.AHrefTag;
import org.webpieces.templating.impl.tags.ExtendsTag;
import org.webpieces.templating.impl.tags.FormTag;
import org.webpieces.templating.impl.tags.CustomTag;
import org.webpieces.templating.impl.tags.HtmlGetTag;
import org.webpieces.templating.impl.tags.HtmlSetTag;

public class HtmlTagLookup {
	
	private Map<String, HtmlTag> tags = new HashMap<>();
	
	@Inject
	public HtmlTagLookup(TemplateConfig config) {
		put(new HtmlSetTag());
		put(new HtmlGetTag());
		put(new ExtendsTag());
		put(new AHrefTag());
		put(new FormTag(config.getDefaultFormAcceptEncoding()));
	}

	protected void put(HtmlTag tag) {
		HtmlTag htmlTag = tags.get(tag.getName());
		if(htmlTag != null)
			throw new IllegalArgumentException("Tag="+tag.getName()+" was already added to the tag map by class="+htmlTag.getClass()+" Your class="+tag.getClass());
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
			if(tag instanceof CustomTag) {
				CustomTag fileTag = (CustomTag) tag;
				fileTag.initialize(templateService);
			}
		}
	}
}
