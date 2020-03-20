package org.webpieces.webserver.tags;

import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.CustomTag;

import javax.inject.Inject;

public class TagOverrideLookupForTesting extends HtmlTagLookup {

	@Inject
	public TagOverrideLookupForTesting(TemplateConfig config, RouterLookup lookup, ConverterLookup converter) {
		super(config, lookup, converter);
		put(new CustomTag("/org/webpieces/webserver/tags/include/custom.tag"));
	}

}
