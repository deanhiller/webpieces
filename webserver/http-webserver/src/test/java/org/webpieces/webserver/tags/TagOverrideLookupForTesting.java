package org.webpieces.webserver.tags;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.CustomTag;

public class TagOverrideLookupForTesting extends HtmlTagLookup {

	@Inject
	public TagOverrideLookupForTesting(TemplateConfig config, RouterLookup lookup) {
		super(config, lookup);
		put(new CustomTag("/org/webpieces/webserver/tags/include/custom.tag"));
	}

}
