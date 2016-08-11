package org.webpieces.templating.impl.tags;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;

import groovy.lang.Closure;

public class RenderTagArgsTag extends TemplateLoaderTag implements HtmlTag {

	@Override
	public String getName() {
		return "renderTagArgs";
	}

	@Override
	protected Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs, Closure<?> body, String srcLocation) {
		if(tagArgs.get("_body") != null)
			throw new IllegalArgumentException("tag "+getName()+" must not define an argument of '_body' as that is reserved for the actual body");
		Map<String, Object> copy = new HashMap<>();
		for(Map.Entry<Object, Object> entry : tagArgs.entrySet()) {
			String key = entry.getKey().toString();
			copy.put(key, entry.getValue());
			if(body != null)
				body.setProperty(key, entry.getValue());
		}
		
		String bodyStr = "";
		if(body != null) {
			bodyStr = ClosureUtil.toString(body);
		}
		copy.put("_body", bodyStr);
		
		return copy;
	}

}
