package org.webpieces.webserver.tags;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.CustomTag;

public class TestTagOverrideLookup extends HtmlTagLookup {

	@Inject
	public TestTagOverrideLookup(TemplateConfig config) {
		super(config);
		put(new CustomTag("/org/webpieces/webserver/basic/includetags/custom.tag"));
	}

}
