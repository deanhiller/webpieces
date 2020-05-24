package org.webpieces.templating.api;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.ctx.api.extension.Tag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public interface HtmlTag extends Tag {

	void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation);

	String getName();
		
}
