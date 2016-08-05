package org.webpieces.templating.api;

import java.util.HashMap;
import java.util.Map;

public class HtmlTagLookup {
	
	private Map<String, HtmlTag> tags = new HashMap<>();
	
	public HtmlTagLookup() {
		
	}

	protected void put(HtmlTag tag) {
		tags.put(tag.getName(), tag);
	}

	public HtmlTag lookup(String tagName) {
		HtmlTag tag = tags.get(tagName);
		return tag;
	}
}
