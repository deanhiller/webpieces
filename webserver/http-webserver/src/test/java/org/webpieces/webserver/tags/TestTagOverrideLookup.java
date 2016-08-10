package org.webpieces.webserver.tags;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.HtmlFileTag;

public class TestTagOverrideLookup extends HtmlTagLookup {

	@Inject
	public TestTagOverrideLookup(TemplateConfig config) {
		super(config);
		put(new HtmlFileTag("/org/webpieces/webserver/tags/custom.tag"));
	}

}
