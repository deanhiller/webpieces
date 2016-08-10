package org.webpieces.templating.impl.tags;

import java.util.Map;

public class RenderPageArgsTag extends TemplateLoaderTag {

	@Override
	public String getName() {
		return "renderPageArgs";
	}

	@Override
	protected Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs) {
		return pageArgs;
	}

}
