package org.webpieces.templating.impl.source;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.Tag;
import org.webpieces.templating.impl.tags.ElseIfTag;
import org.webpieces.templating.impl.tags.ElseTag;
import org.webpieces.templating.impl.tags.IfTag;
import org.webpieces.templating.impl.tags.VerbatimTag;

public class TagLookup {

	private Map<String, Tag> tags = new HashMap<>();
	
	public TagLookup() {
		put(new VerbatimTag());
		put(new IfTag());
		put(new ElseIfTag());
		put(new ElseTag());
	}
	
	protected void put(Tag tag) {
		tags.put(tag.getName(), tag);
	}

	public Tag lookup(String tagName, TokenImpl token) {
		Tag tag = tags.get(tagName);
		return tag;
	}
}
