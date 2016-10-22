package org.webpieces.templating.api;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public interface HtmlTag {

	void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation);

	String getName();
		
}
