package org.webpieces.templating.impl.tags;

import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

/**
 * We could write another CustomTag extending RenderPageArgsTag as well
 * 
 * @author dhiller
 *
 */
public class CustomTag extends RenderTagArgsTag implements HtmlTag {

	private String file;
	private String name;

	public CustomTag(String file) {
		if(!file.endsWith(".tag"))
			throw new IllegalArgumentException("tag file must end in .tag="+file);
		else if(!file.startsWith("/"))
			throw new IllegalArgumentException("tag file path must begin with / which is the root of the classpath");
		this.file = file;
		
		int extensionIndex = file.lastIndexOf(".");
		this.name = file.substring(0, extensionIndex);
		int lastSlashIndex = this.name.lastIndexOf("/");
		if(lastSlashIndex > 0) {
			this.name = this.name.substring(lastSlashIndex+1);
		}
	}

	@Override
	protected String getFilePath(GroovyTemplateSuperclass callingTemplate, Map<Object, Object> args, Closure<?> body, String srcLocation) {
		return file;
	}

	@Override
	public String getName() {
		return name;
	}
}
