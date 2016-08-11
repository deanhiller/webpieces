package org.webpieces.templating.impl.tags;

import java.util.Map;

import groovy.lang.Closure;

public class RenderPageArgsTag extends TemplateLoaderTag {

	@Override
	public String getName() {
		return "renderPageArgs";
	}

	@Override
	protected Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs, Closure<?> body, String srcLocation) {
        if(body != null)
        	throw new IllegalArgumentException("Only #{"+getName()+"/}# can be used.  You cannot do #{"+getName()+"}# #{/"+getName()+"} as the body is not used with this tag. "+srcLocation);
     
		return pageArgs;
	}

}
