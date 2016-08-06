package org.webpieces.templating.api;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.impl.tags.ExtendsTag;
import org.webpieces.templating.impl.tags.HtmlGetTag;
import org.webpieces.templating.impl.tags.HtmlSetTag;

public class HtmlTagLookup {
	
	private Map<String, HtmlTag> tags = new HashMap<>();
	
	public HtmlTagLookup() {
		put(new HtmlSetTag());
		put(new HtmlGetTag());
		put(new ExtendsTag());
	}

	protected void put(HtmlTag tag) {
		tags.put(tag.getName(), tag);
	}

	public HtmlTag lookup(String tagName) {
		HtmlTag tag = tags.get(tagName);
		return tag;
	}
}
