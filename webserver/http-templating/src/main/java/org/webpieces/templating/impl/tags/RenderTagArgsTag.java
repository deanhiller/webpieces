package org.webpieces.templating.impl.tags;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;

public class RenderTagArgsTag extends TemplateLoaderTag implements HtmlTag {

	@Override
	public String getName() {
		return "renderTagArgs";
	}

	@Override
	protected Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs) {
		Map<String, Object> copy = new HashMap<>();
		for(Map.Entry<Object, Object> entry : tagArgs.entrySet()) {
			String key = entry.getKey().toString();
			copy.put(key, entry.getValue());
		}
		return copy;
	}

}
